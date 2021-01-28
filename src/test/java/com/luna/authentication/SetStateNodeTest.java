/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2021 Identity Fusion Inc.
 */
package com.luna.authentication;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.BDDMockito.given;

import java.util.Map;
import java.util.Optional;

import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.ExternalRequestContext.Builder;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.luna.authentication.SetStateNode.Config;

@SuppressWarnings("unchecked")
public class SetStateNodeTest {

    @Mock
    private Config config;
    private SetStateNode node;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        node = new SetStateNode(config);
    }

    @Test
    public void shouldSetAllEntriesFromConfig() {
        ImmutableMap<String, String> attributes = ImmutableMap.of("hello", "world", "example-key", "example-value");
        given(config.keys()).willReturn(attributes);
        Action action = node.process(context());

        assertThat(action).isNotNull();
        assertThat(action.sharedState.asMap()).containsAllEntriesOf(attributes);
    }

    @Test
    public void shouldOverrideExistingValues() {
        ImmutableMap<String, String> attributes = ImmutableMap.of("hello", "world", "example-key", "example-value");
        given(config.keys()).willReturn(attributes);
        Action action = node.process(context(entry("hello", "welt"), entry("example-key", "odd-value")));

        assertThat(action).isNotNull();
        assertThat(action.sharedState.asMap()).containsAllEntriesOf(attributes);
    }

    @Test
    public void shouldAdvanceToNextNode() {
        ImmutableMap<String, String> attributes = ImmutableMap.of("hello", "world");
        given(config.keys()).willReturn(attributes);
        Action action = node.process(context());

        assertThat(action).isNotNull();
        assertThat(action.outcome).isEqualTo("outcome");
    }

    private TreeContext context(Map.Entry<String, Object>... fields) {
        return new TreeContext(json(object(fields)), request(), emptyList(), Optional.empty());
    }

    private ExternalRequestContext request() {
        return new Builder().build();
    }
}