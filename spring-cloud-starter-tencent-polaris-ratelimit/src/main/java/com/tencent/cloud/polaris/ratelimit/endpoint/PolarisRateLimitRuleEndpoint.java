/*
 * Tencent is pleased to support the open source community by making Spring Cloud Tencent available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.cloud.polaris.ratelimit.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.ratelimit.config.PolarisRateLimitProperties;
import com.tencent.polaris.client.pb.RateLimitProto;
import com.tencent.polaris.client.pb.RoutingProto;
import org.apache.commons.lang.StringUtils;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * Endpoint of Polaris RateLimit rule.
 *
 * @author shuiqingliu
 **/
@Endpoint(id = "polaris-ratelimit")
public class PolarisRateLimitRuleEndpoint {

	private final ServiceRuleManager serviceRuleManager;
	private final PolarisRateLimitProperties polarisRateLimitProperties;

	public PolarisRateLimitRuleEndpoint(ServiceRuleManager serviceRuleManager, PolarisRateLimitProperties polarisRateLimitProperties) {
		this.serviceRuleManager = serviceRuleManager;
		this.polarisRateLimitProperties = polarisRateLimitProperties;
	}

	@ReadOperation
	public Map<String, Object> rateLimit(@Selector String namespace, @Selector String service, @Nullable String dstService) {
		Map<String, Object> result = new HashMap<>();
		RateLimitProto.RateLimit rateLimit = serviceRuleManager.getServiceRateLimitRule(namespace, service);
		result.put("properties", polarisRateLimitProperties);
		result.put("namespace", namespace);
		result.put("service", service);
		result.put("rateLimits", parseRateLimitRule(rateLimit));

		if (StringUtils.isEmpty(dstService)) {
			return result;
		}
		List<RoutingProto.Route> routes = serviceRuleManager.getServiceRouterRule(namespace, service, dstService);
		result.put("routes", routes);
		return result;
	}

	private List<Object> parseRateLimitRule(RateLimitProto.RateLimit rateLimit) {
		List<Object> rateLimitRule = new ArrayList<>();
		if (rateLimit == null || CollectionUtils.isEmpty(rateLimit.getRulesList())) {
			return rateLimitRule;
		}

		for (RateLimitProto.Rule rule : rateLimit.getRulesList()) {
			Map<String, Object> ruleMap = new HashMap<>();
			ruleMap.put("id", rule.getId());
			ruleMap.put("priority", rule.getPriority());
			ruleMap.put("resource", rule.getResource());
			ruleMap.put("type", rule.getType());
			ruleMap.put("labels", rule.getLabelsMap());
			ruleMap.put("amounts", rule.getAmountsList());
			ruleMap.put("action", rule.getAction());
			ruleMap.put("disable", rule.getDisable());
			ruleMap.put("report", rule.getReport());
			ruleMap.put("create_time", rule.getCtime());
			ruleMap.put("modify_time", rule.getMtime());
			ruleMap.put("revision", rule.getRevision());
			ruleMap.put("service_token", rule.getServiceToken());
			ruleMap.put("adjuster", rule.getAdjuster());
			ruleMap.put("regex_combine", rule.getRegexCombine());
			ruleMap.put("amount_mode", rule.getAmountMode());
			ruleMap.put("failover", rule.getFailover());
			ruleMap.put("cluster", rule.getCluster());
			rateLimitRule.add(ruleMap);
		}
		return rateLimitRule;
	}

}
