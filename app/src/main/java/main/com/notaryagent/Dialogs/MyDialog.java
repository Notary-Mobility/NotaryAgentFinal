package main.com.notaryagent.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import main.com.notaryagent.Interfaces.onConfirmDialogListener;
import main.com.notaryagent.R;
import main.com.notaryagent.databinding.DialogConfirmBinding;
public class MyDialog {
    private  Type TYPE =Type.MESSAGE;
    Context mContext;
       private String Title="";
       private String Message="";
    private onConfirmDialogListener listener;
    public static MyDialog get(Context context) {
        return new MyDialog(context);
    }
       public MyDialog(Context context) {
            mContext=context;
            setType(Type.MESSAGE);
        }
        public  MyDialog setTitle(String title){
            this.Title=title;
            return this;
        }

        public MyDialog setMessage(String message){
           this.Message=message;
            return this;
        }
    public MyDialog Callback(onConfirmDialogListener listener){
        this.listener=listener;
        return this;
    }

     public MyDialog setType(Type type){
        this.TYPE=type;
            return this;
        }
        public void Show(){
          new MyDialogEx(mContext).show();
        }
        class MyDialogEx extends Dialog {
           public  DialogConfirmBinding binding;

           public MyDialogEx(@NonNull Context context) {
               super(context);
           }


           @Override
           protected void onCreate(Bundle savedInstanceState) {
               super.onCreate(savedInstanceState);
               binding= DataBindingUtil.inflate(getLayoutInflater(), R.layout.dialog_confirm,null,false);
               setContentView(binding.getRoot());
               getWindow().setBackgroundDrawableResource(android.R.color.transparent);
               getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
               getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
               setCanceledOnTouchOutside(false);
               BindView();
           }

           private void BindView() {
               binding.tvOk.setOnClickListener(v->{
                   if (listener!=null){
                       listener.Success();
                   }
                   dismiss();
               });
               binding.tvCancel.setOnClickListener(v->dismiss());
               if (!Title.trim().isEmpty()) {
                   binding.tvTitle.setText(Title);
               }
               if (!Message.trim().isEmpty()) {
                   binding.tvMessage.setText(Message);
               }
               switch (TYPE){
                   case ERROR:
                       binding.tvCancel.setVisibility(View.GONE);
                       binding.tvMessage.setTextColor(Color.RED);
                       break;
                   case CONFIRM:
                       binding.tvCancel.setVisibility(View.VISIBLE);
                       binding.tvMessage.setTextColor(Color.BLACK);
                       break;
                   case MESSAGE:
                       binding.tvCancel.setVisibility(View.GONE);
                       binding.tvMessage.setTextColor(Color.BLACK);
                       break;
               }
           }
    }
    public enum Type{
        MESSAGE,CONFIRM,ERROR,
    }
}
