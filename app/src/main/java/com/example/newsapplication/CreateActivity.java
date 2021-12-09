package com.example.newsapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CreateActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OPEN_IMAGE = 1;

    String article_title, article_abstract, article_body, article_category, article_subtitle;
    EditText TitleArticle;
    EditText AbstractArticle;
    EditText BodyArticle;
    EditText CategoryArticle;
    EditText SubtitleArticle;
    ImageView ArticleImageInput;
    Button btn_save_article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        TitleArticle = findViewById(R.id.ArticleTitleInput);
        AbstractArticle = findViewById(R.id.ArticleAbstractInput);
        BodyArticle = findViewById(R.id.ArticleBodyInput);
        CategoryArticle = findViewById(R.id.ArticleCategoryInput);
        SubtitleArticle = findViewById(R.id.ArticleSubtitleInput);
        ArticleImageInput = findViewById(R.id.ArticleImageInput);

        ((Button)findViewById(R.id.btn_select_image)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i = new Intent();
                 i.setAction(Intent.ACTION_GET_CONTENT);
                 i.addCategory(Intent.CATEGORY_OPENABLE);
                 i.setType("image/*");
                 startActivityForResult(i, REQUEST_CODE_OPEN_IMAGE);
             }
         });


        btn_save_article = findViewById(R.id.btn_save_article);
        btn_save_article.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                article_title = TitleArticle.getText().toString();
                article_abstract = AbstractArticle.getText().toString();
                article_category = CategoryArticle.getText().toString();
                article_body = BodyArticle.getText().toString();
                article_subtitle = SubtitleArticle.getText().toString();
                showToast(article_title);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_OPEN_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    InputStream stream = null;
                    try {
                        Toast.makeText(this, data.getData().toString(), Toast.LENGTH_SHORT).show();
                        stream = getContentResolver().openInputStream(data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        ((ImageView) findViewById(R.id.ArticleImageInput)).setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "User cancelled the selection", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
    }


        private void showToast(String text){
            Toast.makeText(CreateActivity.this, text, Toast.LENGTH_SHORT).show();
        }
    }

