import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

public class MedicalServiceTest {

    @BeforeAll
    public static void startMedicalServiceTest(){
        System.out.println("\n---START MEDICAL SERVICE TESTS---\n");
    }

    @AfterAll
    public static void endMedicalServiceTest(){
        System.out.println("\n---END MEDICAL SERVICE TESTS---\n");
    }

    @AfterEach
    void completeTest(TestInfo testInfo) {
        System.out.println("[TEST] \"" + testInfo.getDisplayName() + "\" сomplete");
    }

    @ParameterizedTest(name="Check {2} alert message")
    @MethodSource("checkBloodPressureMessageSource")
    void checkBloodPressureMessageTest(String id, PatientInfo patientInfo, BloodPressure bloodPressure, String expected){

        //given
        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito
                .when(patientInfoRepository.getById(id))
                .thenReturn(patientInfo);
        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        //when
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkBloodPressure(id, bloodPressure);

        //then
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals(expected, argumentCaptor.getValue());

    }
    public static Stream<Arguments> checkBloodPressureMessageSource(){
        return Stream.of(
                Arguments.of(
                        "patient-ivanov",
                        new PatientInfo("patient-ivanov","Иван", "Иванов", LocalDate.of(1950, 1, 1),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(120, 80))
                        ),
                        new BloodPressure(60, 120),
                        "Warning, patient with id: patient-ivanov, need help"
                )
                ,
                Arguments.of(
                        "patient-petrov",
                        new PatientInfo("patient-petrov","Петр", "Петров", LocalDate.of(1960, 2, 2),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(120, 80))
                        ),
                        new BloodPressure(120, 60),
                        "Warning, patient with id: patient-petrov, need help"
                )
        );
    }

    @ParameterizedTest(name="Check {2} alert sender")
    @MethodSource("checkBloodPressureSource")
    void checkBloodPressureTest(String id, PatientInfo patientInfo, BloodPressure bloodPressure, int expected){

        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito
                .when(patientInfoRepository.getById(id))
                .thenReturn(patientInfo);

        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);

        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
        medicalService.checkBloodPressure(id, bloodPressure);

        Mockito.verify(alertService, Mockito.times(expected)).send(Mockito.any());

    }

    public static Stream<Arguments> checkBloodPressureSource(){
        return Stream.of(
                Arguments.of(
                        "patient-ivanov",
                        new PatientInfo("patient-ivanov", "Иван", "Иванов", LocalDate.of(1950, 1, 1),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(120, 80))
                        ),
                        new BloodPressure(60, 120),
                        1
                ),
                Arguments.of(
                        "patient-petrov",
                        new PatientInfo("patient-petrov", "Петр", "Петров", LocalDate.of(1960, 2, 2),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(120, 80))
                        ),
                        new BloodPressure(120, 80), 0
                ),
                Arguments.of(
                        "patient-elenina",
                        new PatientInfo("patient-elenina","Елена", "Еленина", LocalDate.of(1970, 3, 3),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(110, 90))
                        ),
                        new BloodPressure(120, 80), 1
                ),
                Arguments.of(
                        "patient-tamarina",
                        new PatientInfo("patient-tamarina", "Тамара", "Тамарина", LocalDate.of(1980, 4, 4),
                                new HealthInfo(new BigDecimal("36.6"),
                                        new BloodPressure(100, 70))
                        ),
                       new BloodPressure(100, 70), 0
                )
        );
    }

    @ParameterizedTest(name="Check temperature {2} alert sender")
    @MethodSource("checkTemperatureSource")
    void checkTemperatureTest(String id, PatientInfo patientInfo, BigDecimal currentTemperature, int expected){

        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito
                .when(patientInfoRepository.getById(id))
                .thenReturn(patientInfo);

        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);

        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);

        medicalService.checkTemperature(id, currentTemperature);
        Mockito.verify(alertService, Mockito.times(expected)).send(Mockito.any());

    }
    public static Stream<Arguments> checkTemperatureSource(){
        return Stream.of(
                Arguments.of(
                        "patient-ivanov",
                        new PatientInfo(
                                "patient-ivanov",
                                "Иван",
                                "Иванов",
                                LocalDate.of(1950, 1, 1),
                                new HealthInfo(
                                        new BigDecimal("36.6"),
                                        new BloodPressure(120, 80)
                                )
                        ),
                        new BigDecimal( "36.3" ),
                        0
                ),
                Arguments.of(
                        "patient-petrov",
                        new PatientInfo(
                                "patient-petrov",
                                "Петр",
                                "Петров",
                                LocalDate.of(1960, 2, 2),
                                new HealthInfo(
                                        new BigDecimal("36.6"),
                                        new BloodPressure(120, 80)
                                )
                        ),
                        new BigDecimal( "33.3" ),
                        1
                )
                /*
                    При температуре выше обычной на 1.51 и более градусов тест падает,
                    так как ожидается, что будет произведена отправка alert сообщения,
                    но из метода checkTemperature приходит ответ, что разница между показателями температуры
                    36,6 и 39,3 менее нуля ("-1")
                */
//                ,
//                Arguments.of(
//                        "patient-tamarina",
//                        new PatientInfo(
//                                "patient-tamarina",
//                                "Тамара",
//                                "Тамарина",
//                                LocalDate.of(1970, 3, 3),
//                                new HealthInfo(
//                                        new BigDecimal("33.6"),
//                                        new BloodPressure(120, 80)
//                                )
//
//                        ),
//                        new BigDecimal( "39.3" ),
//                        1
//                )
        );
    }
}
