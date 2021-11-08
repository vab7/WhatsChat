package com.belousov.whatschat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ChatItem extends Fragment {

    ImageView img;

    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseFirestore firestore;

    StorageReference storageReference;

    LinearLayoutManager linearLayoutManager;
    FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> firestoreRecyclerAdapter;

    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.chat_item, container, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = storage.getReference();

        recyclerView = itemView.findViewById(R.id.recycler_view);

        Query query = firestore.collection("Users").whereNotEqualTo("uid", auth.getUid());
        FirestoreRecyclerOptions<FirebaseModel> allUserName = new FirestoreRecyclerOptions
                .Builder<FirebaseModel>().setQuery(query, FirebaseModel.class).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUserName) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull FirebaseModel firebaseModel) {

                noteViewHolder.name.setText(firebaseModel.getName());

//                String uri = firebaseModel.getImage();
//                Log.d("log", " \nChatItem: " + uri);
//                Picasso.get().load(uri).into(img);

                storageReference.child("Images").child(firebaseModel.getUid()).child("pick")
                        .getDownloadUrl().addOnSuccessListener(uri -> {
                    Picasso.get().load(uri.toString()).into(img);
                });

                if (firebaseModel.getStatus().equals("Online")) {
                    noteViewHolder.status.setText(firebaseModel.getStatus());
                    noteViewHolder.status.setTextColor(Color.GREEN);
                } else {
                    noteViewHolder.status.setText(firebaseModel.getStatus());
                }

                noteViewHolder.itemView.setOnClickListener(note -> {
                    Intent intent = new Intent(getActivity(), ChatFragment.class);
                    intent.putExtra("name", firebaseModel.getName());
                    intent.putExtra("uid", firebaseModel.getUid());
                    intent.putExtra("img", firebaseModel.getImage());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_view_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(firestoreRecyclerAdapter);

        return itemView;

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView status;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_user);
            status = itemView.findViewById(R.id.status);
            img = itemView.findViewById(R.id.img_user);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
    }
}
