package com.sample.todolistview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TODOList extends Activity {
	static MyDBHelper helper;

static SQLiteDatabase db;
static ArrayList<Tasks> tasks;
final static int END_CODE = 1;
static ListView list;
static TaskAdapter adapter = null;

private static final int MENU1_ID = 1;
private static final int MENU2_ID = 2;

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

//DB接続
helper = new MyDBHelper(this);
db = helper.getReadableDatabase();
Cursor c = db.query("todolist",
new String[] { "_id", "name", "num", "notice", "is_finished"},
null, null, null, null, "_id DESC", null);

//ListView初期化
list = (ListView)findViewById(R.id.listView1);

tasks = new ArrayList<Tasks>();

//adapterにDBから文字列を追加
boolean isEof = c.moveToFirst();
while(isEof){
Tasks t = new Tasks(
Integer.parseInt(c.getString(0)),
c.getString(1),
c.getString(2),
c.getString(3),
Integer.parseInt(c.getString(4)));
tasks.add(t);
isEof = c.moveToNext();
}
c.close();

//list.setAdapter(adapter);
adapter = new TaskAdapter(this, R.layout.list_row, tasks);
list.setAdapter(adapter);

//コンテキストメニューのためにリストビューを登録
registerForContextMenu(list);

//ボタンを登録
Button addButton = (Button)findViewById(R.id.button1);
ClickListener listener = new ClickListener();
addButton.setOnClickListener(listener);

//アイテムクリック
list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
@Override
public void onItemClick(AdapterView<?> parent, View view, int position, long id){
Intent intent = new Intent(TODOList.this, TodoEdit.class);
Tasks t = tasks.get(position);

//intentにデータをつめる
intent.putExtra("DATA", t);
intent.putExtra("position", position);

//詳細Activityのスタート
startActivityForResult(intent, 0);
}
});
}

//詳細画面から戻ってきたときの処理
public void onActivityResult(int requestCode, int resCode, Intent it){

	switch(resCode)
	{
	case Activity.RESULT_OK:
		//サブ画面からデータの受け取り
		Tasks t = (Tasks)it.getSerializableExtra("DATA");
		int pos = (Integer) it.getIntExtra("position", 0);

		//インスタンスの入れ替え
		String removeName = tasks.get(pos).getName();
		tasks.set(pos, t);
		//DB更新
		helper.onUpdate(db, t);
		//アダプタの書き換え
		//adapter.remove(removeName);
		//adapter.insert(t.getName(), pos);
		adapter = new TaskAdapter(getApplicationContext(), R.layout.list_row, tasks);
		list.setAdapter(adapter);
//adapter.notifyDataSetChanged();
}
}

//メニュー
public boolean onCreateOptionsMenu(Menu menu){
//メニューアイテムの追加
menu.add(Menu.NONE, MENU1_ID, 0, R.string.menu_save).setIcon(android.R.drawable.ic_menu_view);
menu.add(Menu.NONE, MENU2_ID, 1, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
return super.onCreateOptionsMenu(menu);
}

//メニューが選択されたとき
public boolean onOptionsItemSelected(MenuItem item){
switch(item.getItemId()){
case MENU1_ID:
return true;
case MENU2_ID:
return true;
default:
return true;
}
}

//コンテキストメニュー
public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
super.onCreateContextMenu(menu, view, menuInfo);
AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo)menuInfo;
menu.setHeaderTitle(tasks.get(adapterinfo.position).getName());
menu.add(0, END_CODE, 0, R.string.context_delete);
}

//コンテキストメニュークリック時のリスナ
public boolean onContextItemSelected(MenuItem item){
AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();

switch(item.getItemId()){
//削除
case END_CODE:
String removeName = tasks.get(info.position).getName();
//DBから削除
helper.onDelete(db, tasks.get(info.position));
//ArrayListから削除
tasks.remove(info.position);
//アダプタの書き換え
adapter.remove(removeName);
adapter.notifyDataSetChanged();
return true;
default:
Toast.makeText(this, "default", Toast.LENGTH_SHORT).show();
return super.onContextItemSelected(item);
}
}

//ボタンクリック時のリスナ
class ClickListener implements OnClickListener {
public void onClick(View v){
switch(v.getId())
{
//addボタンが押されたら
case R.id.button1:
//テキストのインスタンスを取得
EditText editText = (EditText)findViewById(R.id.editText1);
String strText = editText.getText().toString();

if(strText.length() != 0)
{
//まずEditTextのテキストを削除
editText.setText("");

//新しいIDを取得
String query = "SELECT max(_id)+1 AS max_id FROM todolist";
Cursor c = db.rawQuery(query, null);
c.moveToFirst();
int id = 1;
id = c.getInt(0);

//DBに追加
db.execSQL(
"INSERT INTO todolist(_id, name, num, notice, is_finished) values("+
id+","+"'"+strText+"', '', '', 0);");

//DBに追加した内容を取得
c = db.query("todolist",
new String[] { "_id", "name", "num", "notice", "is_finished"},
null, null, null, null, "_id DESC", "0,1");
c.moveToFirst();

Tasks t = new Tasks(
Integer.parseInt(c.getString(0)),
c.getString(1),
c.getString(2),
c.getString(3),
Integer.parseInt(c.getString(4)));
tasks.add(0, t);

//リストに追加
adapter = new TaskAdapter(getApplicationContext(), R.layout.list_row, tasks);
list.setAdapter(adapter);

}
}
}
}

public class TaskAdapter extends ArrayAdapter{
private ArrayList<Tasks> items;
private LayoutInflater inflater;

@SuppressWarnings("unchecked")
public TaskAdapter(Context context, int textViewResourceId, ArrayList<Tasks> items) {
super(context, textViewResourceId, items);
this.items = items;
this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
}

@SuppressWarnings("unchecked")
public void insert(String str, int position){
super.insert(str, position);
}

@SuppressWarnings("unchecked")
public void remove(String str){
super.remove(str);
}

@SuppressWarnings("unchecked")
public void add(String str){
super.add(str);
}

@Override
public View getView(int position, View convertView, ViewGroup parent){
View view = convertView;
if(view == null){
view = inflater.inflate(R.layout.list_row, null);
}

Tasks item = (Tasks)items.get(position);
if(item != null){
//TextViewに関する事柄
TextView todoName = (TextView)view.findViewById(R.id.todo_name);
if(todoName != null){
todoName.setText(item.getName());
}
//CheckBoxに関する事柄
CheckBox ck = (CheckBox)view.findViewById(R.id.todo_check);
final int p = position;
//CheckBox Checkリスナ
ck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
if(isChecked){
tasks.get(p).setIsFinished(1);
} else {
tasks.get(p).setIsFinished(0);
}
//CheckBox更新
helper.onUpdate(db, tasks.get(p));
}
});
if(item.getIsFinished() == 1){
ck.setChecked(true);
} else {
ck.setChecked(false);
}
}
return view;
}
}

public static class MyDBHelper extends SQLiteOpenHelper{
static Context con;
public MyDBHelper(Context context){
super(context, "todo", null, 1);
con = context;
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int nreVersion){

}

@Override
public void onCreate(SQLiteDatabase db){
//table create
db.execSQL(
"CREATE TABLE todolist("+
" _id INTEGER not null,"+
" name text not null,"+
" num text,"+
" notice text,"+
" is_finished INTEGER not null DEFAULT 0"+
");");
}

//データの更新
public void onUpdate(SQLiteDatabase db, Tasks task){
db.execSQL(
"UPDATE todolist SET "+
" name='"+task.getName()+"', "+
" num='"+task.getNum()+"', "+
" notice='"+task.getNotice()+"', "+
" is_finished="+task.getIsFinished()+
" WHERE _id="+task.getId()+";");
}

//データの削除
public void onDelete(SQLiteDatabase db, Tasks task){
db.execSQL("DELETE FROM todolist WHERE _id="+task.getId());
}

}

@Override
protected void onResume() {
super.onResume();
}

@Override
protected void onPause() {
super.onPause();
}

}

class Tasks implements Serializable{
private static final long serialVersionUID = 8023254505558453097L;
int id;
String name;
String num;
String notice;
int is_finished;

Tasks(int id, String name, String num, String notice, int is_finished){
this.id = id;
this.name = name;
this.num = num;
this.notice = notice;
this.is_finished = is_finished;
}

public int getId(){
return this.id;
}

public String getName(){
return this.name;
}
public void setName(String name){
this.name = name;
}

public String getNum(){
return this.num;
}
public void setNum(String num){
this.num = num;
}

public String getNotice(){
return this.notice;
}
public void setNotice(String notice){
this.notice = notice;
}

public int getIsFinished(){
return this.is_finished;
}
public void setIsFinished(int flg){
this.is_finished = flg;
}

}