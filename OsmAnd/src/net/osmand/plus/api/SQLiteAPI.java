package net.osmand.plus.api;

public interface SQLiteAPI {
	interface SQLiteConnection {
		void close();
		SQLiteCursor rawQuery(String sql, String[] selectionArgs);
		void execSQL(String query);
		void execSQL(String query, Object[] objects);
		SQLiteStatement compileStatement(String string);
		void setVersion(int newVersion);
		int getVersion();
	}
	
	interface SQLiteCursor {
		boolean moveToFirst();
		boolean moveToNext();

		/**
		 * Takes parameter value (zero based)
		 */
		String getString(int ind);

		double getDouble(int ind);
		long getLong(int ind);
		int getInt(int ind);
		void close();
	}
	
	interface SQLiteStatement {
		// 1 based argument
		void bindString(int i, String filterId);

		// 1 based argument
		void bindNull(int i);

		void execute();
		void close();
	}
	
	SQLiteConnection getOrCreateDatabase(String name, boolean readOnly);
}