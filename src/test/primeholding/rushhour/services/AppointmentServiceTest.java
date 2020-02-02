package primeholding.rushhour.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.repositories.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
class AppointmentServiceTest {

    @MockBean
    private AppointmentRepository repository;

    @MockBean
    private ActivityService activityService;

    private List<Appointment> appointments;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUpTest(){
        this.appointments = new ArrayList<>();

        Appointment firstAppointment = new Appointment();
        firstAppointment.setId(1L);
        firstAppointment.setStartDate(LocalDateTime.now());
        firstAppointment.setEndDate(LocalDateTime.now().plusMinutes(60));

        Appointment secondAppointment = new Appointment();
        secondAppointment.setId(2L);
        secondAppointment.setStartDate(LocalDateTime.now());
        secondAppointment.setEndDate(LocalDateTime.now().plusMinutes(60));

        Appointment thirdAppointment = new Appointment();
        thirdAppointment.setId(3L);
        thirdAppointment.setStartDate(LocalDateTime.now());
        thirdAppointment.setEndDate(LocalDateTime.now().plusMinutes(60));

        this.appointments.add(firstAppointment);
        this.appointments.add(secondAppointment);
        this.appointments.add(thirdAppointment);

        Mockito.when(this.repository.findAll()).thenReturn(this.appointments);

        this.appointmentService = new AppointmentService(this.activityService,this.repository);
    }

    @Test
    void findAllAppointments(){
        //Act:
        List<Appointment> actualAppointments = this.appointmentService.findAll();
        //Assert:
        assertEquals(this.appointments.size(),actualAppointments.size());
    }

    @Test
    void getEntityById(){
        //Arrange:
        Mockito.when(this.repository.getOne(1L)).thenReturn(this.appointments.get(0));
        //Act:
        Appointment entity = this.appointmentService.getEntity(1L);
        //Assert:
        assertEquals(this.appointments.get(0),entity);
    }
}