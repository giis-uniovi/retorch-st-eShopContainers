package giis.eshopcontainers.e2e.functional;


import giis.retorch.orchestration.classifier.EmptyInputException;
import giis.retorch.orchestration.generator.OrchestrationGenerator;
import giis.retorch.orchestration.orchestrator.NoFinalActivitiesException;
import giis.retorch.orchestration.scheduler.NoTGroupsInTheSchedulerException;
import giis.retorch.orchestration.scheduler.NotValidSystemException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@Disabled("Exclude to execute this class when pushing the SUT")
class RetorchGenerateJenkinfileTest {
    @Test
    void testGenerateJenkinsfile() throws NoFinalActivitiesException, NoTGroupsInTheSchedulerException, EmptyInputException, IOException, URISyntaxException, NotValidSystemException, ClassNotFoundException {
        OrchestrationGenerator orch= new OrchestrationGenerator();
        orch.generateJenkinsfile("giis.eshopcontainers.e2e.functional.tests","EShopOnContainers", "./");
    }
}