package com.maiia.pro.service;

import com.maiia.pro.entity.Appointment;
import com.maiia.pro.entity.Availability;
import com.maiia.pro.entity.TimeSlot;
import com.maiia.pro.repository.AppointmentRepository;
import com.maiia.pro.repository.AvailabilityRepository;
import com.maiia.pro.repository.TimeSlotRepository;
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

    public List<Availability> findByPractitionerId(Integer practitionerId) {
        if (practitionerId == null) {
            throw new IllegalArgumentException("Practitioner ID shouldn't be null");
        }
        return availabilityRepository.findByPractitionerId(practitionerId);
    }

    @Transactional
    public List<Availability> saveAvailabilities(final List<Availability> availabilities) {
        if (availabilities == null || availabilities.isEmpty()) {
            return Collections.emptyList();
        }
        return Streamable.of(availabilityRepository.saveAll(availabilities)).toList();
    }

    public List<Availability> generateAvailabilities(Integer practitionerId) {
        if (practitionerId == null) {
            throw new IllegalArgumentException("Practitioner ID shouldn't be null");
        }
        List<TimeSlot> timeSlots = timeSlotRepository.findByPractitionerId(practitionerId);
        List<Appointment> appointments = appointmentRepository.findByPractitionerId(practitionerId);
        List<TimeSlot> availableTimeSlots = getAvailableTimeSlots(timeSlots, appointments);

        List<Availability> availabilities = availableTimeSlots.stream()
                .filter(this::hasMinimumDuration)
                .map(timeSlot -> splitTimeSlotIntoAvailabilities(timeSlot, practitionerId))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return saveAvailabilities(availabilities);
    }

    /**
     * Check if the given timeslot is long enough to schedule at least one appointment, based on the default duration
     */
    private boolean hasMinimumDuration(final TimeSlot timeSlot) {
        return Duration.between(timeSlot.getStartDate(), timeSlot.getEndDate()).toMinutes() >= availabilityDuration;
    }

    /**
     * Removes time intervals from a list of TimeSlot where appointments may have been scheduled
     *
     * @param timeSlots    List of time slots to process
     * @param appointments List of appointments that may overlap with time slots
     * @return A list of time slots with no appointment overlaps
     */
    private List<TimeSlot> getAvailableTimeSlots(final List<TimeSlot> timeSlots, final List<Appointment> appointments) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return Collections.emptyList();
        }
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>(timeSlots);
        }
        return timeSlots.stream()
                .map(timeSlot -> getAvailableTimeSlotsForSingle(timeSlot, appointments))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Removes time intervals from a single TimeSlot where appointments may have been scheduled
     *
     * @param timeSlot     The time slot to process
     * @param appointments List of appointments that may overlap with the time slot
     * @return A list of time slots with no appointment overlaps
     */
    private List<TimeSlot> getAvailableTimeSlotsForSingle(final TimeSlot timeSlot, final List<Appointment> appointments) {
        final List<Appointment> overlappingAppointments = appointments.stream()
                .filter(appointment -> isAppointmentOverlappingTimeSlot(appointment, timeSlot))
                .sorted(Comparator.comparing(Appointment::getStartDate))
                .collect(Collectors.toList());

        if (overlappingAppointments.isEmpty()) {
            return Collections.singletonList(timeSlot);
        }
        return calculateAvailableSlots(timeSlot, overlappingAppointments);
    }

    /**
     * @param timeSlot           the TimeSlot to process
     * @param sortedAppointments list of Appointment sorted by starting date
     * @return a list of TimeSlot in the range of the given Timeslot, where no Appointments are scheduled yet
     */
    private static List<TimeSlot> calculateAvailableSlots(TimeSlot timeSlot, List<Appointment> sortedAppointments) {
        List<TimeSlot> availableSlots = new ArrayList<>();
        LocalDateTime currentDateTime = timeSlot.getStartDate();

        for (Appointment appointment : sortedAppointments) {
            // add a time slot between the current date and the start of the next appointment
            if (appointment.getStartDate().isAfter(currentDateTime)) {
                availableSlots.add(
                        TimeSlot.builder()
                                .startDate(currentDateTime)
                                .endDate(appointment.getStartDate())
                                .build());
            }
            // Update current time to the end of the appointment if it's later
            if (appointment.getEndDate().isAfter(currentDateTime)) {
                currentDateTime = appointment.getEndDate();
            }
        }

        // add a last timeslot if the end of the last appointment happened before the end of the original timeslot
        if (currentDateTime.isBefore(timeSlot.getEndDate())) {
            availableSlots.add(
                    TimeSlot.builder()
                            .startDate(currentDateTime)
                            .endDate(timeSlot.getEndDate())
                            .build());
        }
        return availableSlots;
    }

    /**
     * check if an appointment overlap with a given timeslot
     */
    private static boolean isAppointmentOverlappingTimeSlot(Appointment appointment, TimeSlot timeSlot) {
        return appointment.getEndDate().isAfter(timeSlot.getStartDate()) &&
                appointment.getStartDate().isBefore(timeSlot.getEndDate());
    }

    /**
     * Split a time slot into a maximum of availabilities, based on the default availability duration
     */
    private List<Availability> splitTimeSlotIntoAvailabilities(TimeSlot timeSlot, Integer practitionerId) {
        List<Availability> availabilities = new ArrayList<>();
        LocalDateTime current = timeSlot.getStartDate();
        while (current.plusMinutes(availabilityDuration).compareTo(timeSlot.getEndDate()) <= 0) {
            Availability availability = Availability.builder()
                    .practitionerId(practitionerId)
                    .startDate(current)
                    .endDate(current.plusMinutes(availabilityDuration))
                    .build();
            availabilities.add(availability);
            current = current.plusMinutes(availabilityDuration);
        }
        return availabilities;
    }
}
