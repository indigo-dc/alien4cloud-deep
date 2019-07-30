package es.upv.indigodc.service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDeploymentsResponse extends AbstractResponse {

    protected List<Link> links;
    protected List<DeploymentOrchestrator> content;
    protected Page page;
}
