import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter<T>(
    @LayoutRes private val itemLayout: Int,
    private val itemListener: RecyclerViewItemClickListener<T>,
) :
    RecyclerView.Adapter<RecyclerViewAdapter<T>.ViewHolder>()
{
	private val list = arrayListOf<T>()

    interface RecyclerViewItemClickListener<T>
    {
        fun onItemClick(v: View?, selectedItem: T, position: Int)
        fun onItemLongClick(v: View?, selectedItem: T, position: Int): Boolean
        fun onBindViewHolder(view: View, item: T)
    }

    fun load(list: List<T>)
    {
        this.list.clear()
        this.list.addAll(list)

        notifyDataSetChanged()
    }

    //region RecyclerView.Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)

        val view = layoutInflater.inflate(itemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val item = list[position]

        holder.render(item)
    }

    override fun getItemCount(): Int = list.size

    //endregion

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener
    {
        fun render(item: T)
        {
            view.setOnClickListener(this@ViewHolder)
            view.setOnLongClickListener(this@ViewHolder)

            itemListener.onBindViewHolder(view, item)
        }

        override fun onClick(v: View?)
        {
            itemListener.onItemClick(v, list[layoutPosition], layoutPosition)
        }

        override fun onLongClick(v: View?): Boolean
        {
            return itemListener.onItemLongClick(v, list[layoutPosition], layoutPosition)
        }
    }
}

//    Code to use in Activity/Fragment
//
//    private lateinit var rvAdapter: RecyclerViewAdapter<T>
// 
//    private fun initRecyclerViewAdapter()
//    {
//        rvAdapter = RecyclerViewAdapter(R.layout.item, this)
//
//        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//
//        binding.rvList.layoutManager = layoutManager
//        binding.rvList.adapter = rvAdapter
//    }