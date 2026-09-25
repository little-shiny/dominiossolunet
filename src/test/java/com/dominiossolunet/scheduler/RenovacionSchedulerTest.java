package com.dominiossolunet.scheduler;

import com.dominiossolunet.service.RenovacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RenovacionSchedulerTest {

    @Mock
    private RenovacionService renovacionService;

    @InjectMocks
    private RenovacionScheduler scheduler;

    @Test
    void ejecutarRenovaciones_debeLlamarAlServicio() {

        scheduler.ejecutarRenovaciones();

        verify(renovacionService).procesarAvisos();
    }
}