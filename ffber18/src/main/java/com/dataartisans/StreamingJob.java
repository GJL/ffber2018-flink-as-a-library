/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dataartisans;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * Demo Job Flink Forward Berlin 2018.
 */
public class StreamingJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setRestartStrategy(RestartStrategies.fixedDelayRestart(Integer.MAX_VALUE, 0));

		env.addSource(new TestSourceFunction()).name("Non-parallel Source").forceNonParallel()
				.startNewChain()
				.rebalance()
				.map(new TestMapFunction())
				.addSink(new TestSinkFunction());

		env.execute("Demo Job Flink Forward Berlin 2018");
	}

	static class TestSinkFunction implements SinkFunction<Integer> {
		@Override
		public void invoke(final Integer value, final Context context) {
		}
	}

	static class TestSourceFunction implements ParallelSourceFunction<Integer> {

		private volatile boolean canceled;

		@Override
		public void run(final SourceContext<Integer> ctx) throws Exception {
			while (!canceled && !Thread.interrupted()) {
				synchronized (ctx.getCheckpointLock()) {
					ctx.collect(1);
					Thread.sleep(100);
				}
			}
		}

		@Override
		public void cancel() {
			canceled = true;
		}
	}

	private static class TestMapFunction implements MapFunction<Integer, Integer> {
		@Override
		public Integer map(final Integer value) throws Exception {
			return value;
		}
	}
}
