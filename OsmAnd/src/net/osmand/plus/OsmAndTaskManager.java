package net.osmand.plus;

import android.os.AsyncTask;

public class OsmAndTaskManager {
	OsmAndTaskManager() {
	}

	public <Params, Progress, Result> OsmAndTask runInBackground(
			OsmAndTaskRunnable<Params, Progress, Result> r, Params... params) {
		InternalTaskExecutor<Params, Progress, Result> exec = new InternalTaskExecutor<>(r);
//		r.exec = exec;
		exec.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		return exec;
	}

	private static class InternalTaskExecutor<Params, Progress, Result>
			extends AsyncTask<Params, Progress, Result>
			implements OsmAndTask {
		private final OsmAndTaskRunnable<Params, Progress, Result> run;
		
		private InternalTaskExecutor(OsmAndTaskRunnable<Params, Progress, Result> r){
			this.run = r;
		}
		
		@Override
		protected Result doInBackground(Params... params) {
			return run.doInBackground(params);
		}
		
		@Override
		protected void onPreExecute() {
			run.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Result result) {
			run.onPostExecute(result);
		}
		
		@Override
		protected void onProgressUpdate(Progress... values) {
			run.onProgressUpdate(values);
		}
	}

	public interface OsmAndTask {
		boolean isCancelled();
	}
	
	public static abstract class OsmAndTaskRunnable<Params, Progress, Result> {
//		OsmAndTask exec;

		protected void onPreExecute() {}
		protected abstract Result doInBackground(Params... params);
		protected void onPostExecute(Result result) {}
		void onProgressUpdate(Progress... values) {}
	}
}