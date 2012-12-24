package org.rr.commons.utils.zip;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Split {
    private ZipInputStream in;

    public Split(ZipInputStream in) {
        this.in = in;
    }

    // Processes the given portion of the file.
    // Called simultaneously from several threads.
    // Use your custom return type as needed, I used String just to give an example.
    public String processPart(ZipInputStream in) throws Exception {
        return "Some result";
    }

    // Creates a task that will process the given portion of the file,
    // when executed.
    public Callable<String> processPartTask(final ZipInputStream in) {
        return new Callable<String>() {
            public String call() throws Exception {
                return processPart(in);
            }
        };
    }

    // Splits the computation into chunks of the given size,
    // creates appropriate tasks and runs them using a 
    // given number of threads.
	public void processAll(int noOfThreads, ZipInputStream in) throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		int count = cores > 1 ? cores - 1 : 1;
        java.util.List<Callable<String>> tasks = new ArrayList<Callable<String>>(cores - 1);
        for(int i = 0; i < count; i++) {
            tasks.add(processPartTask(in));
        }
        ExecutorService es = Executors.newFixedThreadPool(count);
        
        java.util.List<Future<String>> results = es.invokeAll(tasks);
        es.shutdown();

        // use the results for something
        for(Future<String> result : results)
            System.out.println(result.get());
    }

}