package com.example.kris.handwriting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.kris.handwriting.paint.PaintType
import com.example.kris.handwriting.paint.PaintTypeChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 */
class ItemListDialogFragment(val listener: PaintTypeChangedListener) : BottomSheetDialogFragment() {

    var paintToolbarView: RecyclerView? = null

    val toolList = listOf(
        R.drawable.paint_tool_eraser,
        R.drawable.paint_tool_pen,
        R.drawable.paint_tool_brush,
        R.drawable.paint_tool_marker
    )

    var selectedPosition = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_item_list_dialog_list_dialog, container, false)
        paintToolbarView = rootView.findViewById(R.id.paintToolbar)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        paintToolbarView?.adapter = ItemAdapter(toolList)
    }

    private inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_item_list_dialog_list_dialog_item, parent, false)) {
        val paintTool: ImageButton = itemView.findViewById<ImageButton>(R.id.tool)

        init {
            itemView.setOnClickListener {
                Log.d("kriss-OnClickListener", "selectedPosition=$selectedPosition," +
                        "adapterPosition=$adapterPosition,")
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                paintToolbarView?.adapter?.notifyItemChanged(selectedPosition)
                paintToolbarView?.adapter?.notifyItemChanged(adapterPosition)
                selectedPosition = adapterPosition

                listener.onTypeChanged(PaintType.fromInt(selectedPosition))
            }
        }

        fun onBind(position: Int) {
            paintTool.isSelected = (position == selectedPosition)
            paintTool.setImageResource(toolList[position])
        }
    }

    private inner class ItemAdapter(private val items: List<Int>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(position)
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

}