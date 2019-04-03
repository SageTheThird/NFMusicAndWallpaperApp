package com.homie.nf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    RecyclerView recyclerView;
    Context context;
    ArrayList<String> itemsArray=new ArrayList<>();
    ArrayList<String> urlsArray=new ArrayList<>();

    public void  update(String name, String url){
        itemsArray.add(name);
        urlsArray.add(url);
        notifyDataSetChanged();
    }




    public myAdapter(RecyclerView recyclerView, Context context, ArrayList<String> itemsArray,ArrayList<String> urlsArray) {
        this.recyclerView = recyclerView;
        this.context = context;
        this.itemsArray = itemsArray;
        this.urlsArray=urlsArray;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int iCr) {//to create views for list item
        View view= LayoutInflater.from(context).inflate(R.layout.item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int iBi) {
    //initialize elements if individual item
        viewHolder.nameofFile.setText(itemsArray.get(iBi));

    }

    @Override
    public int getItemCount() { //return  the no of items
        return itemsArray.size()  ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView nameofFile;

        public ViewHolder(@NonNull View itemView) {//represents individual list items
            super(itemView);
            nameofFile=itemView.findViewById(R.id.playlist_textView);
          itemView.setOnClickListener(new View.OnClickListener(){


              @Override
              public void onClick(View v) {

                  int position=recyclerView.getChildLayoutPosition(v);
                  Intent intent=new Intent();
                  intent.setType(Intent.ACTION_VIEW);
                  intent.setData(Uri.parse(urlsArray.get(position)));
                  context.startActivity(intent);


              }
          });
         }
    }


}
