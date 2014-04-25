package hgo.btprint4;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
//import android.widget.Filter.FilterResults;

//see http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html

public class FileListAdapter extends ArrayAdapter<PrintFileDetails> implements Filterable{

	private List<PrintFileDetails> printfilesList;
	private Context context;
	private Filter printfileFilter;
	private List<PrintFileDetails> origPrintfilesList;
	
	public FileListAdapter(List<PrintFileDetails> printtfilesList, Context ctx) {
		super(ctx, R.layout.img_row_layout, printtfilesList);
		this.printfilesList = printtfilesList;
		this.context = ctx;
		this.origPrintfilesList = printtfilesList;
	}
	
	public int getCount() {
		return printfilesList.size();
	}

	public PrintFileDetails getItem(int position) {
		return printfilesList.get(position);
	}

	public long getItemId(int position) {
		return printfilesList.get(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		PrintfileHolder holder = new PrintfileHolder();
		
		// First let's verify the convertView is not null
		if (convertView == null) {
			// This a new view we inflate the new layout
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.img_row_layout, null);
			// Now we can fill the layout with the right values
			TextView tv = (TextView) v.findViewById(R.id.name);
			TextView distView = (TextView) v.findViewById(R.id.dist);

			
			holder.printfileNameView = tv;
			holder.distView = distView;
			
			v.setTag(holder);
		}
		else 
			holder = (PrintfileHolder) v.getTag();
		
		PrintFileDetails p = printfilesList.get(position);
		//holder.planetNameView.setText(p.getName());
		//holder.distView.setText("" + p.getDistance());
		
		return v;
	}

	public void resetData() {
		printfilesList = origPrintfilesList;
	}
	
	
	/* *********************************
	 * We use the holder pattern        
	 * It makes the view faster and avoid finding the component
	 * **********************************/
	
	private static class PrintfileHolder {
		public TextView printfileNameView;
		public TextView distView;
	}
	

	
	/*
	 * We create our filter	
	 */
	
	@Override
	public Filter getFilter() {
		if (printfileFilter == null)
			printfileFilter = new PrintfileFilter();
		
		return printfileFilter;
	}

	private class PrintfileFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0) {
				// No filter implemented we return all the list
				results.values = origPrintfilesList;
				results.count = origPrintfilesList.size();
			}
			else {
				// We perform filtering operation
				List<PrintFileDetails> nPrintfileList = new ArrayList<PrintFileDetails>();
				
				for (PrintFileDetails p : printfilesList) {
					if (p.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
						nPrintfileList.add(p);
				}
				
				results.values = nPrintfileList;
				results.count = nPrintfileList.size();

			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			
			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0)
				notifyDataSetInvalidated();
			else {
				printfilesList = (List<PrintFileDetails>) results.values;
				notifyDataSetChanged();
			}
			
		}
	}

}
