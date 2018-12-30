package com.example.pkg.rxjavaexample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import static android.support.constraint.Constraints.TAG;

public class DataSource {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Disposable> disposables;

    private final Retrofit retrofit;
    private final UsersService userService;
    private final PlacesService placesService;

    public DataSource(RecyclerView.LayoutManager mLayoutManager, RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        this.mLayoutManager = mLayoutManager;
        this.disposables = new ArrayList<>();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://my-json-server.typicode.com/evandroferreiras/demo/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userService = retrofit.create(UsersService.class);
        placesService = retrofit.create(PlacesService.class);
    }

    public void getUsers() {
        userService.listUsers()
                .flatMap((Function<List<User>, ObservableSource<String[]>>) users -> {
                    ArrayList<String> myDataset = new ArrayList<>();

                    for (User user: users ) {
                        myDataset.add(user.name);
                    }
                    return Observable.just(myDataset.toArray(new String[0]));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
    }

    public void getUsersWithPlace() {

        userService.listUsers()
                .flatMapIterable(users -> users)
                .flatMap(user -> placesService.getPlaceById(user.placeId),
                        (user, place) -> user.name + " - " + place.title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    ArrayList<String> dataSet = new ArrayList<String>();

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(String s) {
                        dataSet.add(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ",e );
                    }

                    @Override
                    public void onComplete() {
                        for (Disposable disposable : disposables ) {
                            if (!disposable.isDisposed()){
                                disposable.dispose();
                            }

                        }
                        updateAdapter(dataSet.toArray(new String[0]));

                    }
                });
    }

    @NonNull
    private Observer<String[]> getObserver() {
        return new Observer<String[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(String[] strings) {
                updateAdapter(strings);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ",e );
            }

            @Override
            public void onComplete() {
                for (Disposable disposable : disposables ) {
                    if (!disposable.isDisposed()){
                        disposable.dispose();
                    }

                }
            }
        };
    }


    private void updateAdapter(String[] strings) {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(strings);
        mRecyclerView.setAdapter(mAdapter);
    }
}


interface PlacesService {
    @GET("places/{id}")
    Observable<Place> getPlaceById(@Path("id") int placeId);
}

interface UsersService {
    @GET("users")
    Observable<List<User>> listUsers();
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