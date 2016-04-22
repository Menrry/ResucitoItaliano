package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.alexkolpa.fabtoolbar.FabToolbar;

import java.util.Locale;

import it.cammino.risuscito.dialogs.InputTextDialogFragment;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.utils.ThemeUtils;

public class CustomLists extends Fragment implements InputTextDialogFragment.SimpleInputCallback, SimpleDialogFragment.SimpleCallback {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String[] titoliListe;
    private int[] idListe;
    protected DatabaseCanti listaCanti;
    private int listaDaCanc, idDaCanc, indDaModif;
    private ListaPersonalizzata celebrazioneDaCanc;
    private String titoloDaCanc;
//    private int prevOrientation;
    private ViewPager mViewPager;
    private FabToolbar mFab;
    public ImageView fabEdit, fabDelete;
    private View rootView;
    private static final String PAGE_EDITED = "pageEdited";
    public static final int TAG_CREA_LISTA = 111;
    public static final int TAG_MODIFICA_LISTA = 222;
    private TabLayout tabs;
    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tabs_layout_with_fab, container, false);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_custom_lists);

        mLUtils = LUtils.getInstance(getActivity());

        //crea un istanza dell'oggetto DatabaseCanti
        listaCanti = new DatabaseCanti(getActivity());

        updateLista();

        // Create the adapter that will return a fragment for each of the three
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (savedInstanceState != null)
            indDaModif = savedInstanceState.getInt(PAGE_EDITED, 0);
        else
            indDaModif = 0;

        tabs = (TabLayout) rootView.findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);

        ImageButton buttonAddLista = (ImageButton) rootView.findViewById(R.id.fab_add_lista);
        Drawable drawable = DrawableCompat.wrap(buttonAddLista.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        buttonAddLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.lista_add_desc)
//                        .positiveText(R.string.dialog_chiudi)
//                        .negativeText(R.string.cancel)
//                        .input("", "", false, new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
//                            }
//                        })
//                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                //to hide soft keyboard
////                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
////                                        .hideSoftInputFromWindow(materialDialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("titolo", materialDialog.getInputEditText().getText().toString());
//                                bundle.putBoolean("modifica", false);
//                                indDaModif = 2 + idListe.length;
//                                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
//                                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
//                            }
//                        })
//                        .onNegative(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                //to hide soft keyboard
////                                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
////                                        .hideSoftInputFromWindow(materialDialog.getInputEditText().getWindowToken(), 0);
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//                dialog.setCancelable(false);
                new InputTextDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "NEW_LIST")
                        .title(R.string.lista_add_desc)
                        .positiveButton(R.string.dialog_chiudi)
                        .negativeButton(R.string.cancel)
                        .show();
                //to show soft keyboard
//                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        rootView.findViewById(R.id.fab_pulisci).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());
//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.dialog_reset_list_title)
//                        .content(R.string.reset_list_question)
//                        .positiveText(R.string.confirm)
//                        .negativeText(R.string.dismiss)
//                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
//                                        .getView().findViewById(R.id.button_pulisci).performClick();
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .onNegative(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.setCancelable(false);
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "RESET_LIST")
                        .title(R.string.dialog_reset_list_title)
                        .content(R.string.reset_list_question)
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .show();
            }
        });

        ImageButton fab_share = (ImageButton) rootView.findViewById(R.id.fab_condividi);
        drawable = DrawableCompat.wrap(fab_share.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                        .getView().findViewById(R.id.button_condividi).performClick();
            }
        });

        fabEdit = (ImageButton) rootView.findViewById(R.id.fab_edit_lista);
        drawable = DrawableCompat.wrap(fabEdit.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
                Bundle bundle = new Bundle();
                bundle.putInt("idDaModif", idListe[mViewPager.getCurrentItem() - 2]);
                bundle.putBoolean("modifica", true);
                indDaModif = mViewPager.getCurrentItem();
                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_MODIFICA_LISTA);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
            }
        });

        fabDelete = (ImageButton) rootView.findViewById(R.id.fab_delete_lista);
        drawable = DrawableCompat.wrap(fabDelete.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), android.R.color.white));
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
//                prevOrientation = getActivity().getRequestedOrientation();
//                Utility.blockOrientation(getActivity());

                listaDaCanc = mViewPager.getCurrentItem() - 2;
                idDaCanc = idListe[listaDaCanc];
                SQLiteDatabase db = listaCanti.getReadableDatabase();

                String query = "SELECT titolo_lista, lista"
                        + "  FROM LISTE_PERS"
                        + "  WHERE _id = " + idDaCanc;
                Cursor cursor = db.rawQuery(query, null);

                cursor.moveToFirst();
                titoloDaCanc = cursor.getString(0);
                celebrazioneDaCanc = (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(cursor.getBlob(1));
                cursor.close();
                db.close();

//                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                        .title(R.string.action_remove_list)
//                        .content(R.string.delete_list_dialog)
//                        .positiveText(R.string.confirm)
//                        .negativeText(R.string.dismiss)
//                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
//                                db.close();
//
//                                updateLista();
//                                mSectionsPagerAdapter.notifyDataSetChanged();
//                                tabs.setupWithViewPager(mViewPager);
//                                mLUtils.applyFontedTab(mViewPager, tabs);
//                                Handler myHandler = new Handler();
//                                final Runnable mMyRunnable2 = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        tabs.getTabAt(0).select();
//                                    }
//                                };
//                                myHandler.postDelayed(mMyRunnable2, 200);
//                                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
//                                        .setAction(R.string.cancel, new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
////					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
//                                                SQLiteDatabase db = listaCanti.getReadableDatabase();
//                                                ContentValues values = new ContentValues();
//                                                values.put("_id", idDaCanc);
//                                                values.put("titolo_lista", titoloDaCanc);
//                                                values.put("lista", ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
//                                                db.insert("LISTE_PERS", "", values);
//                                                db.close();
//
//                                                updateLista();
//                                                mSectionsPagerAdapter.notifyDataSetChanged();
//                                                tabs.setupWithViewPager(mViewPager);
//                                                mLUtils.applyFontedTab(mViewPager, tabs);
//                                                Handler myHandler = new Handler();
//                                                final Runnable mMyRunnable2 = new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        mViewPager.setCurrentItem(listaDaCanc + 2, false);
//                                                    }
//                                                };
//                                                myHandler.postDelayed(mMyRunnable2, 200);
//                                            }
//                                        })
//                                        .setActionTextColor(getThemeUtils().accentColor())
//                                        .show();
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .onNegative(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                                getActivity().setRequestedOrientation(prevOrientation);
//                            }
//                        })
//                        .show();
//                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface arg0, int keyCode,
//                                         KeyEvent event) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK
//                                && event.getAction() == KeyEvent.ACTION_UP) {
//                            arg0.dismiss();
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.setCancelable(false);
                new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), CustomLists.this, "DELETE_LIST")
                        .title(R.string.action_remove_list)
                        .content(R.string.delete_list_dialog)
                        .positiveButton(R.string.confirm)
                        .negativeButton(R.string.dismiss)
                        .show();
            }
        });

        getFab().setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOuterFrame();
            }
        });

        if (savedInstanceState != null) {
            Log.d(getClass().getName(), "onCreateView: RESTORING");
            idDaCanc = savedInstanceState.getInt("idDaCanc", 0);
            titoloDaCanc = savedInstanceState.getString("titoloDaCanc");
            listaDaCanc = savedInstanceState.getInt("listaDaCanc", 0);
            celebrazioneDaCanc = (ListaPersonalizzata) savedInstanceState.getSerializable("celebrazioneDaCanc");
            if (InputTextDialogFragment.findVisible((AppCompatActivity) getActivity(), "NEW_LIST") != null)
                InputTextDialogFragment.findVisible((AppCompatActivity) getActivity(), "NEW_LIST").setmCallback(CustomLists.this);
            if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "RESET_LIST") != null)
                SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "RESET_LIST").setmCallback(CustomLists.this);
            if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "DELETE_LIST") != null)
                SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "DELETE_LIST").setmCallback(CustomLists.this);
        }

        return rootView;
    }

    /**
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_EDITED, indDaModif);
        outState.putString("titoloDaCanc", titoloDaCanc);
        outState.putInt("idDaCanc", idDaCanc);
        outState.putSerializable("celebrazioneDaCanc", celebrazioneDaCanc);
        outState.putInt("listaDaCanc", listaDaCanc);
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.i(getClass().getName(), "requestCode: " + requestCode);
        if ((requestCode == TAG_CREA_LISTA || requestCode == TAG_MODIFICA_LISTA) && resultCode == Activity.RESULT_OK) {
            updateLista();
            mSectionsPagerAdapter.notifyDataSetChanged();
            tabs.setupWithViewPager(mViewPager);
            mLUtils.applyFontedTab(mViewPager, tabs);
            Handler myHandler = new Handler();
            final Runnable mMyRunnable2 = new Runnable() {
                @Override
                public void run() {
                    tabs.getTabAt(indDaModif).select();
                }
            };
            myHandler.postDelayed(mMyRunnable2, 200);
        }
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            Fragment fragment = mSectionsPagerAdapter.getRegisteredFragment(i);
            if (fragment != null && fragment.isVisible())
                fragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public FabToolbar getFab() {
        if (mFab == null) {
            mFab = (FabToolbar) rootView.findViewById(R.id.fab_pager);
            mFab.setColor(getThemeUtils().accentColor());
        }
        return mFab;
    }

    private void showOuterFrame() {
        View outerFrame = rootView.findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFab().hide();
                hideOuterFrame();
            }
        });
        outerFrame.setVisibility(View.VISIBLE);
    }

    private void hideOuterFrame() {
        final View outerFrame = rootView.findViewById(R.id.outerFrame);
        outerFrame.setOnClickListener(null);
        outerFrame.setVisibility(View.GONE);
    }

    private void updateLista() {

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT titolo_lista, lista, _id"
                + "  FROM LISTE_PERS A"
                + "  ORDER BY _id ASC";
        Cursor cursor = db.rawQuery(query, null);

        int total = cursor.getCount();
//	    Log.i("RISULTATI", total+"");

        titoliListe = new String[total];
        idListe = new int[total];

        cursor.moveToFirst();
        for (int i = 0; i < total; i++) {
//    		Log.i("LISTA IN POS[" + i + "]:", cursor.getString(0));
            titoliListe[i] =  cursor.getString(0);
            idListe[i] = cursor.getInt(2);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CantiParolaFragment();
                case 1:
                    return new CantiEucarestiaFragment();
                default:
                    Bundle bundle=new Bundle();
//            	Log.i("INVIO", "idLista = " + idListe[position - 2]);
                    bundle.putInt("idLista", idListe[position - 2]);
                    ListaPersonalizzataFragment listaPersFrag = new ListaPersonalizzataFragment();
                    listaPersFrag.setArguments(bundle);
                    return listaPersFrag;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2 + titoliListe.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = getActivity().getResources().getConfiguration().locale;
            switch (position) {
                case 0:
                    return getString(R.string.title_activity_canti_parola).toUpperCase(l);
                case 1:
                    return getString(R.string.title_activity_canti_eucarestia).toUpperCase(l);
                default:
                    return titoliListe[position - 2].toUpperCase(l);
            }
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

    @Override
    public void onPositive(@NonNull String tag, @NonNull MaterialDialog dialog) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "NEW_LIST":
                Bundle bundle = new Bundle();
                bundle.putString("titolo", dialog.getInputEditText().getText().toString());
                bundle.putBoolean("modifica", false);
                indDaModif = 2 + idListe.length;
                startActivityForResult(new Intent(getActivity(), CreaListaActivity.class).putExtras(bundle), TAG_CREA_LISTA);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold_on);
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag, @NonNull MaterialDialog dialog) {}
    @Override
    public void onNeutral(@NonNull String tag, @NonNull MaterialDialog dialog) {}

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "RESET_LIST":
                mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())
                        .getView().findViewById(R.id.button_pulisci).performClick();
                break;
            case "DELETE_LIST":
                SQLiteDatabase db = listaCanti.getReadableDatabase();
                db.delete("LISTE_PERS", "_id = " + idDaCanc, null);
                db.close();

                updateLista();
                mSectionsPagerAdapter.notifyDataSetChanged();
                tabs.setupWithViewPager(mViewPager);
                mLUtils.applyFontedTab(mViewPager, tabs);
                Handler myHandler = new Handler();
                final Runnable mMyRunnable2 = new Runnable() {
                    @Override
                    public void run() {
                        tabs.getTabAt(0).select();
                    }
                };
                myHandler.postDelayed(mMyRunnable2, 200);
                Snackbar.make(getActivity().findViewById(R.id.main_content), getString(R.string.list_removed) + titoloDaCanc + "'!", Snackbar.LENGTH_LONG)
                        .setAction(R.string.cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//					    	Log.i("INDICE DA CANC", listaDaCanc+" ");
                                SQLiteDatabase db = listaCanti.getReadableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("_id", idDaCanc);
                                values.put("titolo_lista", titoloDaCanc);
                                values.put("lista", ListaPersonalizzata.serializeObject(celebrazioneDaCanc));
                                db.insert("LISTE_PERS", "", values);
                                db.close();

                                updateLista();
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                tabs.setupWithViewPager(mViewPager);
                                mLUtils.applyFontedTab(mViewPager, tabs);
                                Handler myHandler = new Handler();
                                final Runnable mMyRunnable2 = new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewPager.setCurrentItem(listaDaCanc + 2, false);
                                    }
                                };
                                myHandler.postDelayed(mMyRunnable2, 200);
                            }
                        })
                        .setActionTextColor(getThemeUtils().accentColor())
                        .show();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}
}
