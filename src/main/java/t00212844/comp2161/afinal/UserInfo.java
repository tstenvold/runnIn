package t00212844.comp2161.afinal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.angads25.toggle.LabeledSwitch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class UserInfo extends Fragment {

    ImageView avatar;
    TextView weightUnit;
    TextView heightUnit;
    EditText name;
    EditText weight;
    EditText height;
    EditText dob;
    LabeledSwitch labeledSwitch;
    Uri avatarpath;

    public UserInfo() {
        // Required empty public constructor
    }

    public static UserInfo newInstance(String param1, String param2) {
        return new UserInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        labeledSwitch = view.findViewById(R.id.switch_units);
        avatar = view.findViewById(R.id.avatarImageView);
        FloatingActionButton detailInfo = view.findViewById(R.id.detailsInfo);
        weightUnit = view.findViewById(R.id.weightUnit);
        name = view.findViewById(R.id.editName);
        weight = view.findViewById(R.id.editWeight);
        height = view.findViewById(R.id.editHeight);
        dob = view.findViewById(R.id.editTextDate);

        Button savebutton = view.findViewById(R.id.saveUserInfo);

        savebutton.setOnClickListener(v -> savePreferences());

        detailInfo.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.info))
                .setMessage(getString(R.string.personalInfo))
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> dialog.cancel()).show());

        avatar.setOnClickListener(v -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(photoPicker, 0);
        });

        labeledSwitch.setOnToggledListener((labeledSwitch, isOn) -> toggleUnits(isOn));

        dob.setOnClickListener(v -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(Objects.requireNonNull(dateFormat.parse(dob.getText().toString())));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DatePickerDialog mDatePicker = new DatePickerDialog(requireActivity(), (view1, year, month, dayOfMonth) -> {
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dob.setText(dateFormat1.format(myCalendar.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            mDatePicker.show();
        });
        loadPreferences();
        return view;
    }

    private void toggleUnits(boolean isOn) {
        if (!isOn) {
            weightUnit.setText(R.string.pound);
        } else {
            weightUnit.setText(R.string.kilo);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                avatarpath = imageReturnedIntent.getData();
                InputStream ims;
                FileOutputStream baos;

                try {
                    ims = requireContext().getContentResolver().openInputStream(avatarpath);
                    Bitmap bmpImage = BitmapFactory.decodeStream(ims);
                    baos = new FileOutputStream(requireContext().getFilesDir() + "/" + getString(R.string.avatarpath));
                    if (bmpImage == null) {
                        Toast.makeText(getContext(), getString(R.string.invalidImage), Toast.LENGTH_SHORT).show();
                    } else {
                        bmpImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                avatar.setImageURI(avatarpath);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private void savePreferences() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.units), labeledSwitch.isOn());
        editor.putString(getString(R.string.name), name.getText().toString());
        editor.putString(getString(R.string.weight), weight.getText().toString());
        editor.putString(getString(R.string.birthday), dob.getText().toString());
        editor.putString(getString(R.string.height), height.getText().toString());

        if (avatarpath != null) {
            editor.putString(getString(R.string.avatar), avatarpath.getPath());
        }
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        labeledSwitch.setOn(pref.getBoolean(getString(R.string.units), true));
        toggleUnits(labeledSwitch.isOn());
        name.setText(pref.getString(getString(R.string.name), ""));
        weight.setText(pref.getString(getString(R.string.weight), ""));
        height.setText(pref.getString(getString(R.string.height), ""));
        dob.setText(pref.getString(getString(R.string.birthday), ""));

        File avatarFile = new File(requireContext().getFilesDir() + "/" + getString(R.string.avatarpath));
        if (avatarFile.exists()) {
            avatarpath = Uri.parse(avatarFile.getAbsolutePath());
            avatar.setImageURI(avatarpath);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }


    }
}