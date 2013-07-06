package net.noviceworks.lineposter;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SampleActivity extends Activity {
	public static final int ID_SELECT_IMAGE	= 1;
	
	Activity	mActivity;
	EditText	mEtPostText;
	LinePoster	mLinePoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        mActivity = this;
        mEtPostText = (EditText)findViewById( R.id.etPostText );
        
        mLinePoster = new LinePoster( this );
        
        // setSupportNotInstalledDevice()
        // If not installed the Line App...
        // true : delegate process to web app.
        // false(default): if not installed the Line App, the LinePoster's function returns ERRORCODE(LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED).
        mLinePoster.setSupportNotInstalledDevice( false );
        
        // post text
        Button btn_post_text = (Button)findViewById( R.id.btnPostText );
        btn_post_text.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String message = mEtPostText.getText().toString();
				
				//int result = LinePoster.make( mActivity ).postText( message );	// This usage is also correct.
				int result = mLinePoster.postText( message );
				toastResult( result );
			}
        } );
        
        // Post local image
        Button btn_post_local_image = (Button)findViewById( R.id.btnPostLocalImage );
        btn_post_local_image.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				// call gallery
				Intent intent = new Intent( Intent.ACTION_PICK );
				intent.setType( "image/*" );
				mActivity.startActivityForResult( intent, ID_SELECT_IMAGE );
			}
        } );
        
        // Post assets image
        Button btn_post_assets_image = (Button)findViewById( R.id.btnPostAssetsImage );
        btn_post_assets_image.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Specify a path of a image in assets directory.
				int result = mLinePoster.postAssetsImage( "image/test.png" );
				toastResult( result );
			}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sample, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	// post a image in local.
    	if( requestCode == ID_SELECT_IMAGE && resultCode == RESULT_OK ) {
    		// get file path
    		Uri uri = data.getData();
    		String[] columns = { MediaStore.Images.Media.DATA };
    		
    		ContentResolver cr = getContentResolver();    		
    		Cursor c = cr.query( uri, columns, null, null, null );
    		c.moveToFirst();
    		
    		String file_path = c.getString(0);
    		
    		// post
    		int result = mLinePoster.postImage( file_path );
    		toastResult( result );    		
    	}
    }
    
    void toastResult( int result ) {
    	switch( result) {
    	case LinePoster.LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED:
    		Toast.makeText( this, "The LineApp is not installed.", Toast.LENGTH_SHORT ).show();
    		break;
    	case LinePoster.LP_RESULT_ERROR_COPY_ASSETS_IMAGE_FAILED:
    		Toast.makeText( this, "Failed to copy a image in assets directory.", Toast.LENGTH_SHORT ).show();
    		break;
    	}
    }
}