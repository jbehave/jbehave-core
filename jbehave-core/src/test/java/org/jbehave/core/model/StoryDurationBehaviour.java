package org.jbehave.core.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class StoryDurationBehaviour {
	
	@Test
	void shouldTimeout(){
		StoryDuration duration = new StoryDuration(1);
		sleep(2);
		assertThat(duration.update().timedOut(), is(true));
	}

	@Test
	void shouldNotTimeout(){
		StoryDuration duration = new StoryDuration(0);
		sleep(2);
		assertThat(duration.update().timedOut(), is(false));
	}

	private void sleep(int secs) {
		try {
			TimeUnit.SECONDS.sleep(secs);
		} catch (InterruptedException e) {
		}		
	}

}
