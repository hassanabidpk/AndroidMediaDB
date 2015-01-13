package com.example.android.displayingbitmaps.ui;


import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediaGridFragment extends Fragment {

    private static final String LOG_TAG = "MediaGridFragment";

    // Variables related to Media items
    private int imgCount; // number of images
    private int vidCount; // number of videos
    private Bitmap[] thumbnails;
    private String[] imgDisplayNames;
    private String[] imgSize;
    private long[] thumbnailIds;
    private String[] arrPath;

    private ImageWorker imageWorker;

    // Content Resolver

    private ContentResolver mContentResolver;

    private static final String IMAGE_CACHE_DIR = "thumbs";
    // Views

    private GridView gridView;

    // Map and Hash initializations

    Map<Integer,String> mFolderBucket = new HashMap<Integer,String>();
    Map<Integer,Integer> mFolderBucketCount = new HashMap<Integer,Integer>() {
        @Override
        public Integer get(Object key) {
            Integer result = super.get(key);

            if(result == null)
                return 0;
            else
                return  result;
        }
    };

    private Integer[] folderBucketIds;

//    private FolderAdapter folderAdapter;

//    private ImagesInFolderAdapter imagesAdapter;

    public MediaGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(LOG_TAG, "onCreate");
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreateView");

        // Inflate the layout for this fragment

        final View v = inflater.inflate(R.layout.fragment_media_grid, container, false);

        gridView = (GridView) v.findViewById(R.id.gridview);

        mContentResolver = getActivity().getContentResolver();

//        getRootFolders();
//        folderAdapter = new FolderAdapter(getActivity(),getActivity().getContentResolver(),1,1,1);
//
//        gridView.setAdapter(folderAdapter);

        gridView.setOnItemClickListener(new GridViewListener());
        return v;

    }

    private  class GridViewListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//            getMediaInFolder(position);
//
//            imagesAdapter = new ImagesInFolderAdapter(getActivity(),getActivity().getContentResolver(), imgCount,vidCount, thumbnailIds, arrPath);
//            gridView.setAdapter(imagesAdapter);
            gridView.setOnItemClickListener(null);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                //TODO  Clear Cache
                if(imageWorker != null)
                    imageWorker.clearCache();
                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.home:
//                getRootFolders();
//                folderAdapter = new FolderAdapter(getActivity(),getActivity().getContentResolver(),1,1,1);
//
//                gridView.setAdapter(folderAdapter);
//                gridView.setOnItemClickListener(new GridViewListener());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //TODO   :  Start from here

  /*  private void getRootFolders() {


        Log.d(LOG_TAG, "getRootFolders is called");

        String[] IMAGE_PROJECTION = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};


        String[] VIDEO_PROJECTION = {MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME};


        *//*
        * uri :  The URI, using the content:// scheme
        * projection :  a list of which columns to return, Passing null will return all columns, which is inefficient
        * selection:  Filter declaring which rows to return, formateed as an SQL WHERE clause
        * selectionArgs:  You may include ?s in selection, which will be replaced by values from selectionArgs, in the order that they appear in the selection,
        * sortOrder: How to order the rows, formatted as an SQL ORDER BY clause
        * *//*

        Cursor imgCsr = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                null,null,MediaStore.Images.ImageColumns.DATE_ADDED);


        Cursor vidCsr = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                null, null, MediaStore.Video.VideoColumns.DATE_ADDED);


        getMediaIdnName(imgCsr,vidCsr);


    }


    private void getMediaIdnName(Cursor imageCsr, Cursor videoCsr ) {


        // getting Bucket ID and Display Name Column number for both Images and Videos

        int imgBucketIdCol = imageCsr.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
        int imgBucketNameCol = imageCsr.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

        int vidBucketIdCol = videoCsr.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID);
        int vidBucketNameCol = videoCsr.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME);


        // Image and Video count as returned by Cursor

       imgCount = imageCsr.getCount();
       vidCount = videoCsr.getCount();


        for(int i =0; i < imgCount + vidCount; i++) {

            if(i < imgCount) {

                imageCsr.moveToPosition(i);

                // get bucket id and bucket display names from MediaStore db

                int imgBucketId = imageCsr.getInt(imgBucketIdCol);
                String imgBucketName = imageCsr.getString(imgBucketNameCol);

                mFolderBucket.put(imgBucketId,imgBucketName);
                mFolderBucketCount.put(imgBucketId, mFolderBucketCount.get(imgBucketId) + 1);

                Log.d(LOG_TAG, "Folder name : " + imgBucketName  + " for id : " + imgBucketId);

            } else {

                videoCsr.moveToPosition(i - imgCount);

                int vidBucketId = videoCsr.getInt(vidBucketIdCol);
                String vidBucketName = videoCsr.getString(vidBucketNameCol);

                mFolderBucket.put(vidBucketId,vidBucketName);
                mFolderBucketCount.put(vidBucketId, mFolderBucketCount.get(vidBucketId) + 1);

                Log.d(LOG_TAG, "Folder name : " + vidBucketName  + " for id : " + vidBucketId);

            }

        }


        folderBucketIds = mFolderBucket.keySet().toArray(new Integer[mFolderBucket.size()]);

        // closing the cusror!!!!
        imageCsr.close();
        videoCsr.close();


    }
*/

    //TODO   :  Step 2

  /*  private void getMediaInFolder(int bucketFolderId) {


        final String[] IMAGE_PROJECTION = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
				*//*
				 * MediaStore.Images.Media.HEIGHT,
				 * MediaStore.Images.Media.WIDTH,
				 *//*MediaStore.Images.Media.SIZE };
        final String[] VIDEO_PROJECTION = { MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        final String vorderBy = MediaStore.Video.Media.DATE_TAKEN;


        // selection and Selection Args for Folders
        final String selection = MediaStore.Images.Media.BUCKET_ID + "=?";
        final String[] selectionArgs = { folderBucketIds[bucketFolderId].toString() };

        final String vselection = MediaStore.Video.Media.BUCKET_ID + "=?";
        final String[] vselectionArgs = { folderBucketIds[bucketFolderId].toString() };


        Cursor imagecursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                selection, selectionArgs, orderBy);
        Cursor videocursor = getActivity().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                vselection, vselectionArgs, vorderBy);


        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        int video_column_index = videocursor.getColumnIndex(MediaStore.Video.Media._ID);
        int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int vdataColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int imgdisplay_name_index = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        int vdisplay_name_index = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
        int imgsize_index = imagecursor.getColumnIndex(MediaStore.Images.Media.SIZE);
        int vduration_index = videocursor.getColumnIndex(MediaStore.Video.Media.DURATION);


        this.imgCount = imagecursor.getCount();
        this.vidCount = videocursor.getCount();
        this.thumbnails = new Bitmap[this.imgCount + this.vidCount];
        this.arrPath = new String[this.imgCount + this.vidCount];
        this.thumbnailIds = new long[this.imgCount + this.vidCount];
        this.imgDisplayNames = new String[this.imgCount
                + this.vidCount];
        this.imgSize = new String[this.imgCount + this.vidCount];

        if (imagecursor != null)
            imagecursor.moveToFirst();

        int i = 0;

        while (true) {
            if (this.imgCount != 0) {

                thumbnailIds[i] = imagecursor.getInt(image_column_index);
                arrPath[i] = imagecursor.getString(dataColumnIndex);
                imgDisplayNames[i] = imagecursor.getString(imgdisplay_name_index);
                imgSize[i] = imagecursor.getString(imgsize_index);

                if (imagecursor.isLast())
                    break;
                imagecursor.moveToNext();
                i++;
            } else
                break;
        }

        if (videocursor != null)
            videocursor.moveToFirst();
        if (this.imgCount != 0)
            i++;

        while (true) {

            if (this.vidCount != 0) {

                thumbnailIds[i] = videocursor.getInt(video_column_index);
                arrPath[i] = videocursor.getString(vdataColumnIndex);
                imgDisplayNames[i] = videocursor
                        .getString(vdisplay_name_index);
                imgSize[i] = videocursor.getString(vduration_index);
                if (videocursor.isLast())
                    break;

                videocursor.moveToNext();
                i++;
            } else
                break;
        }
        if (imagecursor != null)
            imagecursor.close();
        if (videocursor != null)
            videocursor.close();


    }*/

    //TODO   :  Step 3

   /* public class FolderAdapter extends BaseAdapter {

        private static final String LOG = "FolderAdapter";
        private Context mContext;
        private ContentResolver mContentResolver;


        public FolderAdapter(Context context, ContentResolver contentResolver,
                             int nCountImage, int nCountVideo, int FolderClicked) {

            Log.d(LOG,"FolderAdapter Constructor");

            mContext = context;
            mContentResolver = contentResolver;

        }

        @Override
        public int getCount() {
            return folderBucketIds.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewGroup view = (ViewGroup) convertView;

            if (convertView == null) {

                view = (ViewGroup) getActivity().getLayoutInflater().inflate(
                        R.layout.folder_view, null);

                    GridView.LayoutParams layoutParams = new GridView.LayoutParams(
                            dipToPx(117), dipToPx(113));
                    view.setLayoutParams(layoutParams);
                    view.setPadding(dipToPx(1), dipToPx(1), dipToPx(1),
                            dipToPx(1));


            }
            TextView tview1 = (TextView) view.findViewById(R.id.foldertxt);
            ViewGroup frame = (ViewGroup) view.findViewById(R.id.folderimageholder);
            ImageView view1 = (ImageView) view.findViewById(R.id.folderimage);


                try{
                    view1.setImageDrawable(null);
                    frame.setBackgroundResource(R.drawable.android_folder);
                    tview1.setText(mFolderBucket.get(folderBucketIds[position]));
                    Log.v(LOG_TAG, "Folder Name =" + mFolderBucket.get(folderBucketIds[position]));
                }catch(Exception e)
                {
                    e.printStackTrace();
                }


            return view;
        }
    }
*/

    //TODO   :  Step 4

/*
    public class ImagesInFolderAdapter extends  BaseAdapter {

        private Context mContext;
        private ContentResolver mContentResolver;

       public ImagesInFolderAdapter(Context context, ContentResolver contentResolver,
                                    int nCountImage, int nCountVideo, long[] arrayId,
                                    String[] arrayPath) {

           mContext = context;
           mContentResolver = contentResolver;
           imageWorker = new ImageWorker(mContext, mContentResolver,
                   nCountImage, nCountVideo, arrayId, arrayPath,
                   getResources().getDisplayMetrics());

           ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext,
                   IMAGE_CACHE_DIR);

           // Set memory cache to
           // 25% of app memory
           cacheParams.setMemCacheSizePercent(mContext, 0.79f);

           // The ImageFetcher takes care of loading images into our ImageView
           // children asynchronously
           imageWorker.setLoadingImage(R.drawable.empty_photo);
           imageWorker.addImageCache(getFragmentManager(),
                   cacheParams);
           // mImageWorker.clearCache();
       }
        @Override
        public int getCount() {
            return thumbnailIds.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            ViewGroup v = (ViewGroup) convertView;

            if (convertView == null) {
                v = (ViewGroup) getActivity().getLayoutInflater()
                        .inflate(R.layout.single_folder_view, null);

                    v.setLayoutParams(new GridView.LayoutParams(
                            dipToPx(105), dipToPx(68)));
                    v.setPadding(dipToPx(5), dipToPx(5), dipToPx(5),
                            dipToPx(5));

            }

            ImageView view1 = (ImageView) v.findViewById(R.id.thumbnail);
            view1.setScaleType(ImageView.ScaleType.CENTER_CROP);


            if (position < imgCount && imgCount != 0) {
                imageWorker.loadImage(position, view1, thumbnailIds[position],arrPath[position], v, true);

            } else if (vidCount != 0)
            {

                imageWorker.loadImage(position, view1, thumbnailIds[position], arrPath[position], v, false);


            }
            return v;
        }
    }

*/


    private int dipToPx( int sp ) {


        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, getResources().getDisplayMetrics());
    }



}
