package com.bobbypriambodo.anisuke.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.bobbypriambodo.anisuke.contentprovider.AnisukeContentProvider;
import com.bobbypriambodo.anisuke.database.BucketTable;
import com.bobbypriambodo.anisuke.database.FollowingTable;

/**
 * @author Bobby Priambodo
 */
public class AnisukeIntentService extends IntentService {

	// The actions for this service.
	public static final String ACTION_CREATE_SERIES 		= "actionCreateSeries";
	public static final String ACTION_DELETE_SERIES 		= "actionDeleteSeries";
	public static final String ACTION_UPDATE_SERIES 		= "actionUpdateSeries";
	public static final String ACTION_ADD_TO_BUCKET 		= "actionAddToBucket";
	public static final String ACTION_DELETE_FROM_BUCKET 	= "actionDeleteFromBucket";

	public AnisukeIntentService() {
		super("AnisukeIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final String action = intent.getAction();
		// note: the "==" comparison only works
		// if you're inside the same process!
		if (ACTION_CREATE_SERIES == action) {
			onActionCreateSeries(intent);
		} else if (ACTION_DELETE_SERIES == action) {
			onActionDeleteSeries(intent);
		} else if (ACTION_UPDATE_SERIES == action) {
			onActionUpdateSeries(intent);
		} else if (ACTION_ADD_TO_BUCKET == action) {
			onActionAddToBucket(intent);
		} else if (ACTION_DELETE_FROM_BUCKET == action) {
			onActionDeleteFromBucket(intent);
		}
	}

	private void onActionCreateSeries(Intent intent) {
		String title = intent.getStringExtra(FollowingTable.COL_TITLE);
		String episode = intent.getStringExtra(FollowingTable.COL_EPISODE);

		if (TextUtils.isEmpty(title) && TextUtils.isEmpty(episode))
			return;

		ContentValues values = new ContentValues();
		values.put(FollowingTable.COL_TITLE, title);
		values.put(FollowingTable.COL_EPISODE, episode);

		ContentResolver resolver = getContentResolver();
		resolver.insert(AnisukeContentProvider.CONTENT_URI_FOLLOWING, values);
	}

	private void onActionDeleteSeries(Intent intent) {
		long seriesId = intent.getLongExtra(FollowingTable.COL_ID, -1);
		if (seriesId == -1)
			throw new IllegalStateException("Cannot update record with seriesId == -1");

		Uri delUri = ContentUris.withAppendedId(AnisukeContentProvider.CONTENT_URI_FOLLOWING, seriesId);
		ContentResolver resolver = getContentResolver();
		resolver.delete(delUri, null, null);
	}

	private void onActionUpdateSeries(Intent intent) {
		long seriesId = intent.getLongExtra(FollowingTable.COL_ID, -1);
		if (seriesId == -1)
			throw new IllegalStateException("Cannot update record with seriesId == -1");

		String title = intent.getStringExtra(FollowingTable.COL_TITLE);
		String episode = intent.getStringExtra(FollowingTable.COL_EPISODE);

		ContentValues values = new ContentValues();
		values.put(FollowingTable.COL_TITLE, title);
		values.put(FollowingTable.COL_EPISODE, episode);

		Uri updateUri = ContentUris.withAppendedId(AnisukeContentProvider.CONTENT_URI_FOLLOWING, seriesId);
		ContentResolver resolver = getContentResolver();
		resolver.update(updateUri, values, null, null);
	}

	private void onActionAddToBucket(Intent intent) {
		long seriesId = intent.getLongExtra(FollowingTable.COL_ID, -1);
		if (seriesId == -1)
			throw new IllegalStateException("Cannot add to bucket with seriesId == -1");

		Uri queryUri = ContentUris.withAppendedId(AnisukeContentProvider.CONTENT_URI_FOLLOWING, seriesId);
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(queryUri, FollowingTable.PROJECTION_ALL, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			String title = cursor.getString(cursor.getColumnIndex(FollowingTable.COL_TITLE));
			String episode = cursor.getString(cursor.getColumnIndex(FollowingTable.COL_EPISODE));

			ContentValues values = new ContentValues();
			values.put(BucketTable.COL_TITLE, title);
			values.put(BucketTable.COL_EPISODE, episode);

			resolver.insert(AnisukeContentProvider.CONTENT_URI_BUCKET, values);

			values = new ContentValues();
			values.put(FollowingTable.COL_TITLE, title);
			values.put(FollowingTable.COL_EPISODE, Integer.parseInt(episode) + 1);

			Uri updateUri = ContentUris.withAppendedId(AnisukeContentProvider.CONTENT_URI_FOLLOWING, seriesId);
			resolver.update(updateUri, values, null, null);
		}
	}

	private void onActionDeleteFromBucket(Intent intent) {
		long seriesId = intent.getLongExtra(BucketTable.COL_ID, -1);
		if (seriesId == -1)
			throw new IllegalStateException("Cannot delete from bucket with seriesId == -1");

		Uri delUri = ContentUris.withAppendedId(AnisukeContentProvider.CONTENT_URI_BUCKET, seriesId);
		ContentResolver resolver = getContentResolver();
		resolver.delete(delUri, null, null);
	}
}