package com.creative.psc.app.adaptor;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.creative.psc.app.R;
import com.creative.psc.app.util.CustomBitmapPool;
import com.creative.psc.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class PersonnelAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,String>> peopleList;
	private ViewHolder viewHolder;
	private Context con;


	public PersonnelAdapter(Context con , ArrayList<HashMap<String,String>> array){
		inflater = LayoutInflater.from(con);
		peopleList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return peopleList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, final View convertview, ViewGroup parent) {

		View v = convertview;

		if(v == null){
			viewHolder = new ViewHolder();

			v = inflater.inflate(R.layout.people_list_item, parent,false);
			viewHolder.people_image = (ImageView) v.findViewById(R.id.imageView1);
			viewHolder.people_name = (TextView)v.findViewById(R.id.textView1);
			viewHolder.people_sabun = (TextView)v.findViewById(R.id.textView2);
			viewHolder.data3 = (TextView)v.findViewById(R.id.textView3);
			viewHolder.data4 = (TextView)v.findViewById(R.id.textView4);
			viewHolder.data5 = (TextView)v.findViewById(R.id.textView5);
			viewHolder.data6 = (TextView)v.findViewById(R.id.textView6);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		UtilClass.dataNullCheckZero(peopleList.get(position));
		byte[] byteArray =  Base64.decode(peopleList.get(position).get("user_pic"), Base64.DEFAULT) ;
//		Bitmap bmp1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		Glide.with(con).load(byteArray)
				.asBitmap()
				.transform(new CropCircleTransformation(new CustomBitmapPool()))
				.error(R.drawable.no_img)
//				.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
				.into(viewHolder.people_image);

		viewHolder.people_name.setText(peopleList.get(position).get("user_nm").toString());
		String user_cell= peopleList.get(position).get("user_cell").toString();
		if(TextUtils.isEmpty((CharSequence) peopleList.get(position).get("user_cell"))){
			viewHolder.data3.setText("");
		}else{
			viewHolder.data3.setText(user_cell);
		}
		viewHolder.people_sabun.setText(peopleList.get(position).get("user_no").toString());
		viewHolder.data4.setText(peopleList.get(position).get("dept_nm1").toString());
		viewHolder.data5.setText(peopleList.get(position).get("dept_nm2").toString());
		viewHolder.data6.setText(peopleList.get(position).get("j_pos").toString());

		return v;
	}


	public void setArrayList(ArrayList<HashMap<String,String>> arrays){
		this.peopleList = arrays;
	}

	public ArrayList<HashMap<String,String>> getArrayList(){
		return peopleList;
	}


	/*
	 * ViewHolder
	 */
	class ViewHolder{
		ImageView people_image;
		TextView people_name;
		TextView data3;
		TextView data4;
		TextView people_sabun;
		TextView data5;
		TextView data6;

	}


}







