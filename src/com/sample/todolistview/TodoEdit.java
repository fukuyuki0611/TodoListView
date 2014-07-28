package com.sample.todolistview;

import jp.android.booksample.multiactivitysample.MainActivity;
import jp.android.booksample.multiactivitysample.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * 編集画面アクティビティ
 * @author 範行
 *
 */
public class TodoEdit extends Activity implements OnClickListener {

	/**
	 * 編集中の項目ID
	 */
	private long editorId;

	/**
	 *アクティビティ起動時に呼ばれる
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_row);

		// イベントハンドラの設定
		Button operate_save = (Button) findViewById(R.id.operate_save);
		operate_save.setOnClickListener(this);

		// Extraが指定された場合、その設定を反映する
		Intent intent = getIntent();

		if (intent.hasExtra(MainActivity.EXTRA_ID)) {
			EditText editor_title = (EditText) findViewById(R.id.editor_title);
			EditText editor_body = (EditText) findViewById(R.id.editor_body);

			//ID,タイトル、本文
			editorId = intent.getLongExtra(MainActivity.EXTRA_ID, 0);

			editor_title.setText(intent
					.getStringExtra(MainActivity.EXTRA_TITLE));
			editor_body.setText(intent.getStringExtra(MainActivity.EXTRA_BODY));
		}
		else {
			editorId = -1;
		}
	}

	/**
	 * クリック時の挙動
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		//保存
		case R.id.operate_save:
			EditText editor_title = (EditText) findViewById(R.id.editor_title);
			EditText editor_body = (EditText) findViewById(R.id.editor_body);

			//タイトルが未入力
			if(editor_title.getText() == null || editor_title.getText().toString().trim().length() == 0)
			{
				MainActivity.showDialog(this, "警告", "表題が未入力です");
				return;
			}

			// 結果を通知するインテントを作成する
			Intent result = new Intent();

			if (editorId != -1) {
				result.putExtra(MainActivity.EXTRA_ID, editorId);
			}
			result.putExtra(MainActivity.EXTRA_TITLE, editor_title.getText()
					.toString());
			result.putExtra(MainActivity.EXTRA_BODY, editor_body.getText()
					.toString());
			setResult(RESULT_OK, result);
			finish();
			break;

		default:
			break;
		}
	}

}
