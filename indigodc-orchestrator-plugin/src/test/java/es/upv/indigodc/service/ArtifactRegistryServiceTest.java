package es.upv.indigodc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArtifactRegistryServiceTest {

    @Test
    public void getSupportedArtifactTypes_empty() {
        ArtifactRegistryService ars = new ArtifactRegistryService();
        Assertions.assertTrue(ars.getSupportedArtifactTypes().length == 0);
    }
}
