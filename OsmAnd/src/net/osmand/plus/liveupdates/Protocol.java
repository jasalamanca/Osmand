package net.osmand.plus.liveupdates;

/**
 * Created by GaidamakUA on 1/12/16.
 */
public final class Protocol {
	private Protocol(){}

	public static class RankingUserByMonthResponse {
		public UserRankingByMonth[] rows;
	}
	
	public static class RecipientsByMonth {
		public String month;
		public String message;
		float regionBtc;
		int regionCount;
		float btc;
		public Recipient[] rows;
	}
	
	public static class UserRankingByMonth {
		public String user ;
		public int changes;
		int globalchanges;
		int rank;
	}

	// {"month":"2015-11","users":"28363","changes":"673830"}
	public static class TotalChangesByMonthResponse {
		public String month;
		public int users;
		public int changes;
	}

	static class Recipient {
		String osmid;
		int changes;
		float btc;
	}
}
