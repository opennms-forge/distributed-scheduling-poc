package org.opennms.poc.ignite.model.workflows;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.opennms.poc.plugin.api.FieldConfigMeta;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PluginMetadata {

    private String pluginName;
    private WorkflowType pluginType;
    private List<FieldConfigMeta> fieldConfigs;
}
