package com.aulas.personbills;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aulas.personbills.Modal.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.zip.Inflater;


public class IncomeFragment extends Fragment {

    //Firebase Database
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;

    //Recycle View
    private RecyclerView recyclerView;

    //Text View
    private TextView incomeTotalSum;

    //Update edit text
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    //Button for Update and Delete
    private Button btnDelete;
    private Button btnUpdate;

    //Data Item Value
    private String type;
    private String note;
    private int amount;

    private String post_key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_income, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

        incomeTotalSum = myView.findViewById(R.id.income_txt_result);

        recyclerView = myView.findViewById(R.id.recycler_id_income);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalValue = 0;

                for(DataSnapshot mysnapshot:dataSnapshot.getChildren()){
                    Data data = mysnapshot.getValue(Data.class);
                    totalValue += data.getAmount();
                    String stTotalValue = String.valueOf(totalValue);
                    incomeTotalSum.setText(stTotalValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myView;
    }

    @Override
    public void onStart() {

        super.onStart();
        FirebaseRecyclerAdapter<Data, MyViewHolder>adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(
                Data.class, R.layout.income_recycler_data, MyViewHolder.class, mIncomeDatabase
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data modal, final int position) {
                viewHolder.setType(modal.getType());
                viewHolder.setNote(modal.getNote());
                viewHolder.setDate(modal.getDate());
                viewHolder.setAmount(modal.getAmount());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();

                        type = modal.getType();
                        note = modal.getNote();
                        amount = modal.getAmount();

                        updateDataItem();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_income);
            mType.setText(type);
        }

        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_income);
            mNote.setText(note);
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_income);
            mDate.setText(date);
        }

        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_income);
            String stAmount = String.valueOf(amount);
            mAmount.setText(stAmount);
        }

    }

    private void updateDataItem() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.update_data_item, null);
        mydialog.setView(myView);

        edtAmount = myView.findViewById(R.id.amount_edt);
        edtType = myView.findViewById(R.id.type_edt);
        edtNote = myView.findViewById(R.id.note_edt);

        //Set data to edit text
        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myView.findViewById(R.id.btnUpdate);
        btnDelete = myView.findViewById(R.id.btnDelete);

        final AlertDialog dialog = mydialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String mdAmount = String.valueOf(amount);
                mdAmount = edtAmount.getText().toString().trim();

                int myAmount = Integer.parseInt(mdAmount);

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(myAmount, type, note, post_key, mDate);

                mIncomeDatabase.child(post_key).setValue(data);

                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIncomeDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}