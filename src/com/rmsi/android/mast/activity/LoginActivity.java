package com.rmsi.android.mast.activity;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.activity.R.string;
import com.rmsi.android.mast.db.DBController;
import com.rmsi.android.mast.domain.User;
import com.rmsi.android.mast.util.CommonFunctions;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity  
{	
	Context context = this;
	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	User user=null;
	ContentValues values = new ContentValues();
	List<ContentValues> valueList = new ArrayList<ContentValues>();
	CommonFunctions cf = CommonFunctions.getInstance();
	String error_msg,error_tag;
	ProgressDialog ringProgressDialog; 
	String userName,password,role;
	private String SERVER_IP = CommonFunctions.SERVER_IP;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// Creating Shared Prefs and Required Folders
		CommonFunctions.getInstance().Initialize(getApplicationContext());		
		cf.createLogfolder();
		cf.loadLocale(getApplicationContext());

		setContentView(R.layout.activity_login);
		/*	DBController sqllite  = new DBController(context);
		sqllite.onCreate(sqllite.getWritableDatabase());
		boolean isdb = sqllite.checkDataBase();
		sqllite.close();

		if(isdb)
		{
			Toast.makeText(this, "DATABASE Created", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, "DATABASE Creation failed", Toast.LENGTH_SHORT).show();
		}	*/
		//Setting toolbar
		/*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar!=null)
			setSupportActionBar(toolbar);*/

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) 
			{
				if (id == R.id.login || id == EditorInfo.IME_NULL) 
				{
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		//Fetching Logged In USER

		getLoggedUserFromDB();

		Button signInButton = (Button) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				attemptLogin();
			}
		});
	}
	
	private void attemptLogin()
	{
		if(user==null)
		{
			userName=mUsernameView.getText().toString();
			password=mPasswordView.getText().toString();
			if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password))
			{
				user = new User();
				user.setUserName(userName);
				user.setPassword(password);						
				validateUserOnline(user);
			}
			else{
				cf.showMessage(context, "Warning", "Please Enter Your Username and Password");
			}
			//loginAction(true,"");
		}
		else
		{
			loginAction(true,"");
		}			
	}
	
	/////////////////////////////////////////////////////////////////////////
	private void getLoggedUserFromDB()
	{
		DBController sqllite = new DBController(context);
		user = sqllite.getLoggedUser();		
		//########## Setting logged USER
		if(user!=null)
		{
			mUsernameView.setText(user.getUserName());
			mPasswordView.setText(user.getPassword());
			role=user.getRoleName();

			mUsernameView.setEnabled(false);
			mPasswordView.setEnabled(false);
		}
		else
		{
			mUsernameView.setText("");
			mPasswordView.setText("");
		}
		sqllite.close();
	}
	
	private void validateUserOnline(User newUser)
	{
		if(cf.getConnectivityStatus())
		{	
		ringProgressDialog = ProgressDialog.show(new ContextThemeWrapper(context, android.R.style.Theme_Holo), 
				null,getResources().getString(R.string.server_logging_msg), true);
		ringProgressDialog.setCancelable(false);
		ringProgressDialog.setCanceledOnTouchOutside(false);
		new validateUser().execute(newUser);
		}
		else
		{
			cf.showIntenetSettingsAlert(context);
			user = null;
		}
		
	}
	
	
	
	private class validateUser extends AsyncTask<User, Integer, String> 
	{
		protected String doInBackground(User... user) 
		{
			String json_string =null;
			InputStream is = null;
			try 
			{
				String	requestUrl = "http://"+SERVER_IP+"/mast/sync/mobile/user/auth";
				
	    		HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();
	    		conn.setReadTimeout(100000 /* milliseconds */);
	    		conn.setConnectTimeout(15000 /* milliseconds */);
	    		conn.setRequestMethod("POST");
	    		conn.setDoInput(true);
	    		conn.setDoOutput(true);
	    		// Starts the query
	    		conn.connect();
	    		
	    		//Setting parameters 
	    		 OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
	    		 String urlParameters = "email="+userName+"&password="+password;
	    		 writer.write(urlParameters);
	    		 writer.flush();
	    		 
	    		int response = conn.getResponseCode();
	    		
	    		if(response>1)
	    		{
	    			is  = conn.getInputStream();
	    			// Convert the InputStream into a string
	    			json_string = CommonFunctions.getStringFromInputStream(is);
	    		}
			}catch(SocketTimeoutException e1)
			{
				
			}
			catch (Exception e) 
			{
				user = null;
				e.printStackTrace();	 			
			}finally{
				ringProgressDialog.dismiss();
	    		if (is != null) {try {is.close();} catch (Exception e) {}} 
	    	}
			return json_string;    
		}

		protected void onPostExecute(String response) 
		{
			JSONObject Obj = null;
			JSONObject childObj = null;
			values = new ContentValues();
			valueList.clear();
			try {
				if(!TextUtils.isEmpty(response))
				{
					Obj = new JSONObject(response);
					if(Obj.equals(null))
					{
						//cf.addErrorMessage("Json is null", Obj.toString());
						error_msg=getResources().getString(R.string.login_error_msg);
						loginAction(false,error_msg);
					}
					else if(Obj.has("Error"))
					{
						error_msg=getResources().getString(string.login_error_msg);
						loginAction(false,error_msg);
					}
					else{
						if(Obj.has("id"))
						{
							values.put("USER_ID",Obj.get("id").toString());
						}
						if(Obj.has("username"))
						{
							values.put("USER_NAME",Obj.get("username").toString());
						}
						if(Obj.has("password"))
						{
							values.put("PASSWORD",Obj.get("password").toString());
						}
						if(Obj.has("roles"))
						{
							JSONArray jsonarrayForRoles=Obj.getJSONArray("roles");
							childObj=new JSONObject(jsonarrayForRoles.get(0).toString());
							role = childObj.get("name").toString();
							values.put("ROLE_ID",childObj.get("id").toString());
							values.put("ROLE_NAME",childObj.get("name").toString());
						}
						valueList.add(values);
						String tableName = "USER";
						loginAction(true,"Success");
						new DBController(getApplicationContext()).InsertValues(valueList,tableName);
					}
				}
				else{
					error_msg=getResources().getString(string.login_error);
					loginAction(false,error_msg);
					user=null;
				}
			} catch (JSONException e) {
				user = null;
				ringProgressDialog.dismiss();
				error_msg=getResources().getString(string.login_error);
				loginAction(false,error_msg);
				e.printStackTrace();
			}


		}
	}
	
	private void loginAction(boolean loginSuccess ,String msg)
	{
		if(ringProgressDialog!=null)
		ringProgressDialog.dismiss();
		
		if(loginSuccess)
		{
			Intent intentlogin=new Intent(getApplicationContext(),LandingPageActivity.class);
			intentlogin.putExtra("role",role); 
			startActivity(intentlogin);			
		}else{			
			cf.showMessage(context, "Login Failed", msg);
		}
	}
	
	
}
