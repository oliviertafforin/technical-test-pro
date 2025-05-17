package com.maiia.pro.service;

import com.maiia.pro.entity.Appointment;
import com.maiia.pro.entity.Availability;
import com.maiia.pro.entity.TimeSlot;
import com.maiia.pro.repository.AppointmentRepository;
import com.maiia.pro.repository.AvailabilityRepository;
import com.maiia.pro.repository.TimeSlotRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProAvailabilityService {

    @Value("${practitioner.appointment.duration:15}")
    private int availabilityDuration;

    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Autowired
    public ProAvailabilityService(AvailabilityRepository availabilityRepository,
                                  AppointmentRepository appointmentRepository,
                                  TimeSlotRepository timeSlotRepository) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Find all availabilities for a practitioner
     */
    public List<Availability> findByPractitionerId(Integer practitionerId) {
        if (practitionerId == null) {
            throw new IllegalArgumentException("Practitioner ID shouldn't be null");
        }
        return availabilityRepository.findByPractitionerId(practitionerId);
    }

    /**
     * Save a list of availabilities
     */
    @Transactional
    public List<Availability> saveAvailabilities(final List<Availability> availabilities) {
        if (availabilities == null || availabilities.isEmpty()) {
            return Collections.emptyList();
        }
        Iterable<Availability> savedAvailabilities = availabilityRepository.saveAll(availabilities);
        return Streamable.of(savedAvailabilities).toList();
    }

    /**
     * Generate availabilities for a practitioner based on their time slots
     * and existing appointments
     */
    public List<Availability> generateAvailabilities(Integer practitionerId) {
        if (practitionerId == null) {
            throw new IllegalArgumentException("Practitioner ID shouldn't be null");
        }
        List<TimeInterval> timeSlots = timeSlotRepository.findByPractitionerId(practitionerId)
                .stream()
                .map(TimeInterval::new)
                .collect(Collectors.toList());
        List<TimeInterval> occupiedTimeIntervals = getOccupiedTimeIntervals(practitionerId);
        List<TimeInterval> availableTimeSlots = getAvailableTimeSlots(timeSlots, occupiedTimeIntervals);

        //create availabilities based on available time slots
        List<Availability> availabilities = availableTimeSlots.stream()
                .filter(this::hasMinimumDurationForAnAppointment)
                .map(timeInterval -> splitTimeIntervalIntoAvailabilities(timeInterval, practitionerId))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return saveAvailabilities(availabilities);
    }

    /**
     * Gather all time constraints (appointments and existing availabilities) into a single TimeInterval list
     */
    public List<TimeInterval> getOccupiedTimeIntervals(Integer practitionerId) {
            List<TimeInterval> unavailableTimeIntervals = new ArrayList<>();
            // Add existing appointments
            appointmentRepository.findByPractitionerId(practitionerId)
                    .forEach(appointment -> unavailableTimeIntervals.add(new TimeInterval(appointment)));
            // Add existing availabilities
            availabilityRepository.findByPractitionerId(practitionerId)
                    .forEach(availability -> unavailableTimeIntervals.add(new TimeInterval(availability)));
            return unavailableTimeIntervals;
        }

    /**
     * Check if the given TimeInterval is long enough to schedule at least one appointment, based on the default duration
     */
    private boolean hasMinimumDurationForAnAppointment(final TimeInterval timeInterval) {
        return Duration.between(timeInterval.getStartDate(), timeInterval.getEndDate()).toMinutes() >= availabilityDuration;
    }

    /**
     * Removes time intervals from a list of TimeInterval where appointments may have been scheduled
     *
     * @param timeSlots             List of time slots to process
     * @param occupiedTimeIntervals List of time intervals that may overlap with time slots
     * @return A list of time intervals with no existing appointment nor existing availabilities overlaps
     */
    private List<TimeInterval> getAvailableTimeSlots(final List<TimeInterval> timeSlots, final List<TimeInterval> occupiedTimeIntervals) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return Collections.emptyList();
        }
        if (occupiedTimeIntervals == null || occupiedTimeIntervals.isEmpty()) {
            return new ArrayList<>(timeSlots);
        }

        return timeSlots.stream()
                .flatMap(timeSlot -> getAvailableTimeSlotsForSingle(timeSlot, occupiedTimeIntervals).stream())
                .collect(Collectors.toList());
    }

    /**
     * Removes time intervals from a single TimeSlot where appointments may have been scheduled
     *
     * @param timeSlot              The time slot to process
     * @param occupiedTimeIntervals List of time intervals that may overlap with the time slot
     * @return A list of time slots with no appointment overlaps
     */
    private List<TimeInterval> getAvailableTimeSlotsForSingle(final TimeInterval timeSlot, final List<TimeInterval> occupiedTimeIntervals) {
        final List<TimeInterval> overlappingTimeIntervals = occupiedTimeIntervals.stream()
                .filter(timeInterval -> areTimeIntervalsOverlapping(timeInterval, timeSlot))
                .sorted(Comparator.comparing(TimeInterval::getStartDate))
                .collect(Collectors.toList());

        if (overlappingTimeIntervals.isEmpty()) {
            return Collections.singletonList(timeSlot);
        }
        return calculateAvailableSlots(timeSlot, overlappingTimeIntervals);
    }

    /**
     * @param timeSlot            the TimeSlot to process
     * @param sortedTimeIntervals list of TimeInterval sorted by starting date
     * @return a list of TimeSlot in the range of the given Timeslot, where no Appointments are scheduled yet
     */
    private List<TimeInterval> calculateAvailableSlots(TimeInterval timeSlot, List<TimeInterval> sortedTimeIntervals) {
        List<TimeInterval> availableSlots = new ArrayList<>();
        LocalDateTime currentDateTime = timeSlot.getStartDate();

        for (TimeInterval timeInterval : sortedTimeIntervals) {
            // add a time slot between the current date and the start of the next appointment
            if (timeInterval.getStartDate().isAfter(currentDateTime)) {
                availableSlots.add(new TimeInterval(currentDateTime, timeInterval.getStartDate()));
            }
            // Update current time to the end of the appointment if it's later
            if (timeInterval.getEndDate().isAfter(currentDateTime)) {
                currentDateTime = timeInterval.getEndDate();
            }
        }

        // add a last timeslot if the end of the last appointment happened before the end of the original timeslot
        if (currentDateTime.isBefore(timeSlot.getEndDate())) {
            availableSlots.add(new TimeInterval(currentDateTime, timeSlot.getEndDate()));
        }
        return availableSlots;
    }

    /**
     * check if two time intervals overlaps
     */
    private boolean areTimeIntervalsOverlapping(TimeInterval timeInterval1, TimeInterval timeInterval2) {
        return !timeInterval1.getEndDate().isBefore(timeInterval2.getStartDate()) &&
                !timeInterval1.getStartDate().isAfter(timeInterval2.getEndDate());
    }

    /**
     * Split a time interval into a maximum of availabilities, based on the default availability duration
     */
    private List<Availability> splitTimeIntervalIntoAvailabilities(TimeInterval timeInterval, Integer practitionerId) {
        List<Availability> availabilities = new ArrayList<>();
        LocalDateTime current = timeInterval.getStartDate();
        while (current.plusMinutes(availabilityDuration).compareTo(timeInterval.getEndDate()) <= 0) {
            availabilities.add(Availability.builder()
                    .practitionerId(practitionerId)
                    .startDate(current)
                    .endDate(current.plusMinutes(availabilityDuration))
                    .build());
            current = current.plusMinutes(availabilityDuration);
        }
        return availabilities;
    }

    /**
     * Simple class to represent a time interval with start and end dates
     */
    @Getter
    @Setter
    @ToString
    private static class TimeInterval {
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        public TimeInterval(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public TimeInterval(Appointment appointment) {
            this.endDate = appointment.getEndDate();
            this.startDate = appointment.getStartDate();
        }

        public TimeInterval(Availability availability) {
            this.endDate = availability.getEndDate();
            this.startDate = availability.getStartDate();
        }

        public TimeInterval(TimeSlot timeSlot) {
            this.endDate = timeSlot.getEndDate();
            this.startDate = timeSlot.getStartDate();

        }
    }
}
