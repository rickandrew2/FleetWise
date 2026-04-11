package com.fleetwise.common;

import com.fleetwise.alert.AlertRepository;
import com.fleetwise.alert.AlertService;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.UserRepository;
import com.fleetwise.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DevDataLoaderIntegrationTest {

    private DevDataLoader devDataLoader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FuelLogRepository fuelLogRepository;

    @Autowired
    private RouteLogRepository routeLogRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        routeLogRepository.deleteAll();
        fuelLogRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        devDataLoader = new DevDataLoader(
                userRepository,
                vehicleRepository,
                fuelLogRepository,
                routeLogRepository,
                alertRepository,
                alertService,
                passwordEncoder);
    }

    @Test
    void shouldSeedDevDataIdempotentlyAcrossRepeatedRuns() throws Exception {
        devDataLoader.run(new DefaultApplicationArguments(new String[0]));

        long userCountAfterFirstRun = userRepository.count();
        long vehicleCountAfterFirstRun = vehicleRepository.count();
        long fuelLogCountAfterFirstRun = fuelLogRepository.count();
        long routeLogCountAfterFirstRun = routeLogRepository.count();
        long alertCountAfterFirstRun = alertRepository.count();

        assertTrue(userCountAfterFirstRun > 0);
        assertTrue(vehicleCountAfterFirstRun > 0);
        assertTrue(fuelLogCountAfterFirstRun > 0);
        assertTrue(routeLogCountAfterFirstRun > 0);

        devDataLoader.run(new DefaultApplicationArguments(new String[0]));

        assertEquals(userCountAfterFirstRun, userRepository.count());
        assertEquals(vehicleCountAfterFirstRun, vehicleRepository.count());
        assertEquals(fuelLogCountAfterFirstRun, fuelLogRepository.count());
        assertEquals(routeLogCountAfterFirstRun, routeLogRepository.count());
        assertEquals(alertCountAfterFirstRun, alertRepository.count());
    }
}
