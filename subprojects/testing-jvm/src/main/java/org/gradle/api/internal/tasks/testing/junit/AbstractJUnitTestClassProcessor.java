/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.testing.junit;

import org.gradle.api.Action;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.api.internal.tasks.testing.results.AttachParentTestResultProcessor;
import org.gradle.internal.actor.Actor;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJUnitTestClassProcessor<T extends JUnitSpec> implements TestClassProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJUnitTestClassProcessor.class);
    protected final T spec;
    protected final IdGenerator<?> idGenerator;
    protected final Clock clock;
    private final ActorFactory actorFactory;
    private Action<TestClassRunInfo> executor;
    private Actor resultProcessorActor;

    public AbstractJUnitTestClassProcessor(T spec, IdGenerator<?> idGenerator, ActorFactory actorFactory, Clock clock) {
        this.idGenerator = idGenerator;
        this.spec = spec;
        this.actorFactory = actorFactory;
        this.clock = clock;
    }

    @Override
    public void startProcessing(TestResultProcessor resultProcessor) {
        // Build a result processor chain
        TestResultProcessor resultProcessorChain = new AttachParentTestResultProcessor(resultProcessor);
        TestClassExecutionEventGenerator eventGenerator = new TestClassExecutionEventGenerator(resultProcessorChain, idGenerator, clock);

        // Wrap the result processor chain up in a blocking actor, to make the whole thing thread-safe
        resultProcessorActor = actorFactory.createBlockingActor(eventGenerator);
        TestResultProcessor threadSafeResultProcessor = resultProcessorActor.getProxy(TestResultProcessor.class);
        TestClassExecutionListener threadSafeTestClassListener = resultProcessorActor.getProxy(TestClassExecutionListener.class);

        executor = createTestExecutor(threadSafeResultProcessor, threadSafeTestClassListener);
    }

    protected abstract Action<TestClassRunInfo> createTestExecutor(TestResultProcessor threadSafeResultProcessor, TestClassExecutionListener threadSafeTestClassListener);

    @Override
    public void processTestClass(TestClassRunInfo testClass) {
        LOGGER.debug("Executing test class {}", testClass.getTestClassName());
        executor.execute(testClass);
    }

    @Override
    public void stop() {
        resultProcessorActor.stop();
    }
}
