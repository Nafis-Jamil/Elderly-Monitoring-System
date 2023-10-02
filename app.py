import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 
from flask import Flask
from flask import request
from flask import send_file, render_template
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import insert
from flask import jsonify
from flask import Flask, redirect, url_for, render_template, request, flash
from keras.models import load_model
import numpy as np

app=Flask(__name__)
N_time_stamps=100
N_features=9

model = load_model('har_model_left')

app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///User_table.sqlite3'
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

db = SQLAlchemy(app)

class User_table(db.Model):
  id = db.Column(db.Integer, primary_key=True)
  username = db.Column(db.String(100))
  password = db.Column(db.String(100))
  mobile = db.Column(db.String(100))
  child = db.relationship('location_table', backref='user_table', uselist=False)

  def __init__(self, username, password, mobile):
    self.username = username
    self.password = password
    self.mobile = mobile

class location_table(db.Model):
  id = db.Column(db.Integer, primary_key=True)
  username = db.Column(db.String(100))
  longitude = db.Column(db.String(100))
  latitude = db.Column(db.String(100))
  address = db.Column(db.String(300))
  city = db.Column(db.String(100))
  country = db.Column(db.String(100))
  activity = db.Column(db.String(100))
  user_id = db.Column(db.String(100), db.ForeignKey('user_table.username'))
  
@app.route('/')
def index():
  return render_template("index.html")

#activity code

@app.route('/activity', methods=["POST"])
def ppost():
    val=request.get_json()
    data = np.asarray(val, dtype= np.float32).reshape(1, N_time_stamps, N_features)
    result =  model.predict(data)
    final_result= result[0].round(decimals=2)
    maxx= final_result.argmax()
    data= '{"max":'+str(maxx)+'}'
    print(data)
    return data

@app.route('/check_username_password', methods=["POST"])
def check_username_password():
   data=request.get_json()
   u=data['username']
   p=data['password']
  
   found_user = User_table.query.filter_by(username=u).first()
   if found_user:
     if found_user.username == u and found_user.password == p:
       ans = "true"
     else:
       ans = "false"
   else:
     ans = "false"

   return ans

@app.route('/web_check_username_password', methods=["POST"])
def web_check_username_password():
  u=request.form['username']
  p=request.form['password']

  found_user = User_table.query.filter_by(username=u).first()
  if found_user:
    if found_user.username == u and found_user.password == p:
      location = location_table.query.filter_by(user_id=u).first()
      if location:
        return render_template("child_activity.html", username=u, address=location.address, city=location.city, country=location.country,
                            longitude=location.longitude, latitude=location.latitude)
      else:
        return render_template("child_activity.html", username=u)
    else:
      ans = "false"
  else:
    ans = "false"
  return ans

@app.route('/sign_up', methods=["POST"])
def sign_up():
    data= request.get_json()
    u=data['username']
    p=data['password']
    m=data['mobile']

    print(u, p, m)

    found_user = User_table.query.filter_by(username=u).first()
    if found_user:
      ans = "false"
    else:
      new_user = User_table(u, p, m)
      db.session.add(new_user)
      db.session.commit()
      ans = "true"

    return ans

@app.route('/get_number', methods=["POST"])
def get_number():
  data= request.get_json()
  user=data['username']

  found_user = User_table.query.filter_by(username=user).first()
  print(found_user.mobile)
  return found_user.mobile

@app.route('/update_location', methods=["POST"])
def update_location():
  data=request.get_json()
  user=data['username']
  latitude=data['latitude']
  longitude=data['longitude']
  address=data['address']
  city=data['city']
  country=data['country']
  activity=data['activity']
  
  print(data)

  found_user = location_table.query.filter_by(user_id=user).first()
  if found_user:
    found_user.longitude = longitude
    found_user.latitude = latitude
    found_user.address = address
    found_user.city = city
    found_user.country = country
    found_user.activity= activity
    db.session.commit()
    print("hello")
  else:
    search_user = User_table.query.filter_by(username=user).first()
  
    new_location = location_table(username=user,
    longitude = longitude,
    latitude = latitude,
    address = address,
    city = city,
    country = country,
    activity= activity,
    user_table=search_user)
    db.session.add(new_location)
    db.session.commit()
  
  return "true"

@app.route('/get_location', methods=["POST"])
def get_location():
  data=request.get_json()
  user=data['username']
  
  location = location_table.query.filter_by(user_id=user).first()
  if location:
      return jsonify(address=location.address,
                     city=location.city,
                     country=location.country,
                     longitude=location.longitude,
                     latitude=location.latitude,
                     activity=location.activity)
    
  else:
    return 'false'
  



@app.route('/web_get_location', methods=["POST"])
def web_get_location():
  user=request.form['username']
  
  print(user)
  location = location_table.query.filter_by(user_id=user).first()
  if (location):
    
    return render_template("child_activity.html", username=user, address=location.address, city=location.city, country=location.country,
                            longitude=location.longitude, latitude=location.latitude)
  else:
    return render_template("child_activity.html", username=user)


@app.route('/one')
def hello_one():
  return 'Hello One'

@app.route('/post', methods=["POST"])
def hello_post():
  u=request.form['username']
  p=request.form['password']

  user = User_table(u, p)
  db.session.add(user)
  db.session.commit()

  return "success"

@app.route('/delete', methods=["POST"])
def delete():
  u = request.form['username']
  user = User_table.query.filter_by(username=u).first()
  if user:
    db.session.delete(user)
    db.session.commit()
    abc = "successful"
  else:
    abc = "unsuccessful"
  return abc

@app.route('/get', methods=["POST"])
def get_details():
  u = request.form['username']
  found_user = User_table.query.filter_by(username=u).first()
  if found_user is None:
    abc = "not found"
  else:
    abc = (found_user.username + " " + found_user.password)
  return abc

@app.route('/test', methods=["POST"])
def tt():
  a = User_table.query.all()
  print(a)
  return "sdfsd"

@app.route('/two')
def hello_two():
  return 'Hello Two'

if __name__=='__main__':
  with app.app_context():
    db.create_all()
  app.run(host='0.0.0.0')