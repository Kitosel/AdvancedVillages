package pl.kiosel.villages.addons.development;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class DevelopmentSettings {

	private final boolean enabled;
	private final Map<String, DevelopmentNode> nodes;

	public DevelopmentSettings(boolean enabled, Map<String, DevelopmentNode> nodes) {
		this.enabled = enabled;
		this.nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
	}

	public DevelopmentNode getNode(String id) { return id == null ? null : this.nodes.get(id); }
}
