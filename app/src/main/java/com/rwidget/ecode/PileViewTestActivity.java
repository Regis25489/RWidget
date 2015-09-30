package com.rwidget.ecode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rwidget.R;
import com.rwidget.pileview.PileView;

import java.util.ArrayList;
import java.util.List;

public class PileViewTestActivity extends Activity {
    private List<String> mlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test);
        PileView pileView = (PileView) this.findViewById(R.id.baseView);

        mlist = new ArrayList<String>();
        mlist.add("A");
        mlist.add("B");
        mlist.add("C");
        mlist.add("D");
        mlist.add("E");
        final CardsAdapter adapter = new CardsAdapter(this, mlist);
        pileView.setAdapter(adapter);
        pileView.setOnSwipingListener(new PileView.OnSwipingListener() {
            @Override
            public void modifyListData() {
                mlist.add(mlist.get(0));
                mlist.remove(0);
                adapter.notifyDataSetChanged();
            }
        });

    }

    public class CardsAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> mlist;

        public CardsAdapter(Context context, List<String> list) {
            this.mlist = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_pile_view, parent, false);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(mlist.get(position));
            //   Log.i("CardsAdapter", convertView.getLayoutParams() + "---");
            return convertView;
        }

    }
}
