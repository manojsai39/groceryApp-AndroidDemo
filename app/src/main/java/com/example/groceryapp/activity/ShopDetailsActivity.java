package com.example.groceryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Constants;
import com.example.groceryapp.R;
import com.example.groceryapp.adapters.AdapterProductBuyer;
import com.example.groceryapp.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {
    
    private ImageView shopIv;
    private TextView  filterProductTv, cartCountTv;
    private EditText searchProductEt;
    private ImageButton backBtn, cartBtn, callBtn, mapBtn, filterProductBtn, logoutBtn;
    private RecyclerView productRv;

    private String shopUid;
    private String myLatitude, myLongitude;
    private String shopName, shopPhone, shopEmail, shopAddress, shopLatitude, shopLongitude;
    private int count = 0;

    private FirebaseAuth mAuth;

    private ArrayList<ModelProduct> productList;
    private AdapterProductBuyer adapterProductBuyer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        
        shopIv = findViewById(R.id.shopIV);
        filterProductTv = findViewById(R.id.filterProductTV);
        searchProductEt = findViewById(R.id.searchProductET);
        backBtn = findViewById(R.id.backBtn);
        cartBtn = findViewById(R.id.cartBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productRv = findViewById(R.id.productRV);
        cartCountTv = findViewById(R.id.cartCounterTV);
        logoutBtn = findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();

        shopUid = getIntent().getStringExtra("shopUid");
        Log.i("Debug","Yes UID IN TRIGGER");
        Log.i("Debug",shopUid);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
//        loadReviews();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                checkUser();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopDetailsActivity.this, CartActivity.class);
                intent.putExtra("ShopUid", shopUid);
                startActivity(intent);
            }
        });



        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = Constants.productCategories1[which];
                                filterProductTv.setText(selected);
                                if (selected.equals("All")){
                                    loadShopProducts();
                                }
                                else {
                                    adapterProductBuyer.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });



        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductBuyer.getFilter().filter(s);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }
    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(ShopDetailsActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private float ratingSum = 0;
//    private void loadReviews() {
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(shopUid).child("Ratings")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        ratingSum = 0;
//                        for (DataSnapshot ds : dataSnapshot.getChildren()){
//                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
////                            ratingSum = ratingSum+rating;
//                        }
//
//                        long numberOfReview = dataSnapshot.getChildrenCount();
////                        float avgRating = ratingSum/numberOfReview;
////                        ratingBar.setRating(avgRating);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadShopProducts() {
        productList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        productList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductBuyer = new AdapterProductBuyer(ShopDetailsActivity.this, productList);
                        productRv.setAdapter(adapterProductBuyer);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = ""+dataSnapshot.child("name").getValue();
                shopName = ""+dataSnapshot.child("shopName").getValue();
                shopEmail = ""+dataSnapshot.child("email").getValue();
                shopPhone = ""+dataSnapshot.child("phone").getValue();
                shopAddress = ""+dataSnapshot.child("address").getValue();
                shopLatitude = ""+dataSnapshot.child("latitude").getValue();
                shopLongitude = ""+dataSnapshot.child("longitude").getValue();
                String deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();

//                shopNameTv.setText(shopName);
//                emailTv.setText(shopEmail);
//                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
//                addressTv.setText(shopAddress);
//                phoneTv.setText(shopPhone);
//                if (shopOpen.equals("true")){
//                    openCloseTv.setText("Open");
//                }
//                else {
//                    openCloseTv.setText("Closed");
//                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        ref.child(mAuth.getUid()).child("CartItem").child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            cartCountTv.setVisibility(View.VISIBLE);
                            count = (int) dataSnapshot.getChildrenCount();
                            cartCountTv.setText(String.valueOf(count));
                        }else {
                            cartCountTv.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
