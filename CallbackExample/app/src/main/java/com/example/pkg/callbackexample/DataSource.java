package com.example.pkg.callbackexample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class DataSource {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlacesService placesService;
    private UsersService usersService;

    DataSource(RecyclerView.LayoutManager mLayoutManager, RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        this.mLayoutManager = mLayoutManager;
        init();
    }

    private void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://my-json-server.typicode.com/evandroferreiras/demo/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.usersService = retrofit.create(UsersService.class);
        this.placesService = retrofit.create(PlacesService.class);
    }

    public void getUsersWithPlace(){
        usersService.listUsers()
                .enqueue(new Callback<List<User>>()
                {
                    ArrayList<String> myDataset = new ArrayList<>();
                    @Override
                    public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                        List<User> users = response.body();
                        assert users != null;
                        for (final User user : users) {
                            placesService.getPlaceById(user.placeId)
                                    .enqueue(new Callback<Place>(){
                                        @Override
                                        public void onResponse(@NonNull Call<Place> call, @NonNull Response<Place> response) {
                                            Place place = response.body();
                                            assert place != null;
                                            myDataset.add(user.name + " - " + place.title);
                                            updateAdapter(myDataset);
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Place> call, @NonNull Throwable t) {
                                            Log.e("DataSource", "onFailure: ", t );
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                        Log.e("DataSource", "onFailure: ", t );
                    }
                });
    }

    private void updateAdapter(ArrayList<String> myDataset) {
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter mAdapter = new MyAdapter(myDataset.toArray(new String[0]));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void getUsers(){
        usersService.listUsers()
            .enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                    ArrayList<String> myDataset = new ArrayList<>();
                    List<User> users = response.body();
                    assert users != null;
                    for (User user : users) {
                        myDataset.add(user.name);
                    }
                    updateAdapter(myDataset);
                }

                @Override
                public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                    Log.e("DataSource", "onFailure: ", t );
                }
            });
    }

}

interface PlacesService {
    @GET("places/{id}")
    Call<Place> getPlaceById(@Path("id") int placeId);
}

interface UsersService {
    @GET("users")
    Call<List<User>> listUsers();
}

class Place {

    int id;
    String title;

    public Place(int id, String title) {
        this.id = id;
        this.title = title;
    }
}

class User {

    int id;
    String name;
    int placeId;

    public User(int id, String name, int placeId ) {
        this.id = id;
        this.name = name;
        this.placeId = placeId;
    }

}