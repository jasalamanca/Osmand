package net.osmand.plus.api;

import net.osmand.plus.OsmandApplication;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteAPIImpl implements SQLiteAPI {

	private final OsmandApplication app;

	public SQLiteAPIImpl(OsmandApplication app) {
		this.app = app;
	}

	@Override
	public SQLiteConnection getOrCreateDatabase(String name, boolean readOnly) {
		android.database.sqlite.SQLiteDatabase db = app.openOrCreateDatabase(name,
				readOnly ? SQLiteDatabase.OPEN_READONLY : (SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING), null);
		if(db == null) {
			return null;
		}
		return new SQLiteDatabaseWrapper(db) ;
	}
	
	
	public class SQLiteDatabaseWrapper implements SQLiteConnection {
		final android.database.sqlite.SQLiteDatabase ds;

		
		SQLiteDatabaseWrapper(android.database.sqlite.SQLiteDatabase ds) {
			super();
			this.ds = ds;
		}

		@Override
		public int getVersion() {
			return ds.getVersion();
		}

		@Override
		public void close() {
			ds.close();
			
		}

		@Override
		public SQLiteCursor rawQuery(String sql, String[] selectionArgs) {
			final Cursor c = ds.rawQuery(sql, selectionArgs);
			if(c == null) {
				return null;
			}
			return new SQLiteCursor() {
				
				@Override
				public boolean moveToNext() {
					return c.moveToNext();
				}
				
				public String[] getColumnNames() {
					return c.getColumnNames();
				}
				
				@Override
				public boolean moveToFirst() {
					return c.moveToFirst();
				}
				
				@Override
				public String getString(int ind) {
					return c.getString(ind);
				}
				
				@Override
				public void close() {
					c.close();
				}

				@Override
				public double getDouble(int ind) {
					return c.getDouble(ind);
				}

				@Override
				public long getLong(int ind) {
					return c.getLong(ind);
				}

				@Override
				public int getInt(int ind) {
					return c.getInt(ind);
				}

				public byte[] getBlob(int ind) {
					return c.getBlob(ind);
				}
			};
		}

		@Override
		public void execSQL(String query) {
			ds.execSQL(query);			
		}

		@Override
		public void execSQL(String query, Object[] objects) {
			ds.execSQL(query, objects);
		}

		@Override
		public SQLiteStatement compileStatement(String query) {
			final android.database.sqlite.SQLiteStatement st = ds.compileStatement(query);
			if(st == null) {
				return null;
			}
			return new SQLiteStatement() {
				
				@Override
				public void execute() {
					st.execute();
					
				}
				
				@Override
				public void close() {
					st.close();
				}
				
				@Override
				public void bindString(int i, String value) {
					st.bindString(i, value);
					
				}
				
				@Override
				public void bindNull(int i) {
					st.bindNull(i);
				}

				public long simpleQueryForLong() {
					return st.simpleQueryForLong();
				}

				public String simpleQueryForString() {
					return st.simpleQueryForString();
				}

				public void bindLong(int i, long val) {
					st.bindLong(i, val);
				}

				public void bindBlob(int i, byte[] val) {
					st.bindBlob(i, val);
				}
			};
		}

		@Override
		public void setVersion(int newVersion) {
			ds.setVersion(newVersion);
		}

		public boolean isReadOnly() {
			return ds.isReadOnly();
		}

		public boolean isDbLockedByOtherThreads() {
			return ds.isDbLockedByOtherThreads();
		}

		public boolean isClosed() {
			return !ds.isOpen();
		}
		
	}


	public SQLiteConnection openByAbsolutePath(String path, boolean readOnly) {
		// fix http://stackoverflow.com/questions/26937152/workaround-for-nexus-9-sqlite-file-write-operations-on-external-dirs
		android.database.sqlite.SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				readOnly? SQLiteDatabase.OPEN_READONLY : (SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING));
		if(db == null) {
			return null;
		}
		return new SQLiteDatabaseWrapper(db) ;
	}
}
