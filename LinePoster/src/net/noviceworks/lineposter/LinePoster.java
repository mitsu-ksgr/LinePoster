/**
 * LinePoster	Copyright (c) 2013 Mitsuaki Nakada
 * 
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.noviceworks.lineposter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

/**
 * The LinePoster class can post text/image to the line app.
 * <p>
 * The LinePoster supports posting text/image/image in assets(a image will be copied the local).
 * <p>
 * 
 * @author Mitsuaki Nakada
 * Blog: http://blog.livedoor.jp/mitsu_aki/
 */
public class LinePoster {
	//--------------------------------------------------------------------------------
	// Constants
	/** Success. */
	public static final int	LP_RESULT_SUCCEED							= 1;
	/** Error: The LineApp is not installed. */
	public static final int	LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED	= -1;
	/** Error: Failed to copy a image in assets directory. */
	public static final int LP_RESULT_ERROR_COPY_ASSETS_IMAGE_FAILED	= -2;
	/**	Error: Failed to UTF-8 encoding. */
	public static final int	LP_RESULT_ERROR_ENCODE_FAILED				= -3;

	private static final String LIEN_APP_PACKAGE_NAME	= "jp.naver.line.android";
	private static final String LINE_TEXT_URL	= "http://line.naver.jp/msg/text/";
	private static final String LINE_TEXT_APP	= "line://msg/text/";
	private static final String LINE_IMAGE_URL	= "http://line.naver.jp/msg/image/";
	private static final String LINE_IMAGE_APP	= "line://msg/image/";
		
	//--------------------------------------------------------------------------------
	// property
	private final Context mContext;
	// The default behavior of a device when the Line app is not installed.
	private boolean mSupportNotInstalledDevece = false;
	
	/**
	 * Make LinePoster Instance.
	 * @param context
	 * @return	LinePoster instance.
	 */
	static public LinePoster make( Context context ) {
		return new LinePoster( context );
	}


	/**
	 * Construct an empty LinePoster object.
	 * @param context
	 */
	public LinePoster( Context context ) {
		mContext = context;
	}
	
	/** 
	 * Set the operation when if not installed the Line App.
	 *	true  : send web app.
	 *	false : return Error Code (LLT_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED).
	 * @param supported	Whether to support.
	 */
	public void setSupportNotInstalledDevice( boolean supported ) {
		mSupportNotInstalledDevece = supported;
	}

	/**
	 * Post a text message.
	 * @param message
	 * @return	LP_RESULT
	 */
	public int postText( String message ) {
		String msg = encodeUtf8( message );
		if( msg == null  )	return LP_RESULT_ERROR_ENCODE_FAILED;
		boolean line_exist = isInstalledApplication( LIEN_APP_PACKAGE_NAME );
		if( !line_exist && !mSupportNotInstalledDevece ) return LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED;
		String uri = (line_exist?LINE_TEXT_APP:LINE_TEXT_URL) + msg;

		return issueIntent( Uri.parse( uri ) );
	}

	/**
	 * Post a image in local directory.
	 * <p>
	 * 	Image must can load by the Line app.
	 * </p>
	 * 
	 * @param file_path path to image in local directory.
	 * @return	LP_RESULT
	 */
	public int postImage( String file_path ) {
		String img_path = encodeUtf8( file_path );
		if( img_path == null ) return LP_RESULT_ERROR_ENCODE_FAILED;

		boolean line_exist = isInstalledApplication( LIEN_APP_PACKAGE_NAME );
		if( !line_exist && !mSupportNotInstalledDevece ) return LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED;
		String uri = (line_exist?LINE_IMAGE_APP:LINE_IMAGE_URL) + img_path;

		return issueIntent( Uri.parse( uri ) );
	}

	/**
	 * Post a image in assets directory.
	 * <p>
	 *	A image file copied to /data/data/your_app_packagename/files/ directory.
	 *	If exist same file name image, the image file will overwite.
	 * </p>
	 * 
	 * @param file_path	path to image in assets directory.
	 * @return	LP_RESULT
	 */
	public int postAssetsImage( String file_path ) {
		String tex_name = assets2local( file_path );
		if( tex_name == null ) return LP_RESULT_ERROR_COPY_ASSETS_IMAGE_FAILED;
		return postImage( mContext.getFileStreamPath( tex_name ).getAbsolutePath() );
		//return shareImage( context, tex_name );
	}


	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private String assets2local( String file_name ) {
		String[] fname_split = file_name.split( "/" );
		String tex_name = fname_split[ fname_split.length-1 ];

		// if don't want overrite every time, enable the following line.
		//if( mContext.getFileStreamPath( tex_name ).exists() ) return tex_name;

		FileChannel src = null, dst = null;
		try {
			AssetFileDescriptor afd = mContext.getResources().getAssets().openFd( file_name );
			src = afd.createInputStream().getChannel();
			dst = mContext.openFileOutput( tex_name, Context.MODE_WORLD_READABLE ).getChannel();
			dst.transferFrom( src, 0, afd.getLength() );
			return tex_name;
		} catch( IOException e ) {
			e.printStackTrace();
			return null;
		} finally {
			if( src != null ) try { src.close(); } catch(IOException e){}
			if( dst != null ) try { dst.close(); } catch(IOException e){}
		}
	}

 	private int issueIntent( Uri uri ) {
 		try {
			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setData( uri );
			mContext.startActivity( intent );
			return LP_RESULT_SUCCEED;
 		} catch( ActivityNotFoundException e ) {
 			return LP_RESULT_ERROR_LINE_APP_IS_NOT_INSTALLED;
 		}
	}

	private boolean isInstalledApplication( String package_name ) {
		try {
			mContext.getPackageManager().getApplicationInfo( package_name, 0 );
			return true;
		} catch( PackageManager.NameNotFoundException e ) {
			return false;
		}
	}

	private String encodeUtf8( String str ) {
		try {
			return new String( str.getBytes( "UTF-8" ), "UTF-8" );
		} catch( UnsupportedEncodingException e ) {
			return null;
		}
	}
}