package com.creative.psc.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creative.psc.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class BaseAdapter extends android.widget.BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,String>> RowList;
	private ViewHolder viewHolder;
	private Context con;


	public BaseAdapter(Context con , ArrayList<HashMap<String,String>> array){
		inflater = LayoutInflater.from(con);
		RowList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return RowList.size();
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

			v = inflater.inflate(R.layout.search_list_row, parent,false);
			viewHolder.data1 = (TextView)v.findViewById(R.id.textView1);
			viewHolder.data2 = (TextView)v.findViewById(R.id.textView2);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.data1.setText(RowList.get(position).get("data1").toString());
		viewHolder.data2.setText(RowList.get(position).get("data2").toString());

		return v;
	}


	public void setArrayList(ArrayList<HashMap<String,String>> arrays){
		this.RowList = arrays;
	}

	public ArrayList<HashMap<String,String>> getArrayList(){
		return RowList;
	}


	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView data1;
		TextView data2;
	}


}







