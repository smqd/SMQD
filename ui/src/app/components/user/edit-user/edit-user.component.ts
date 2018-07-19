import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { UserService } from '../../../services/user.service';
import { FormGroup, Validators, FormBuilder, FormControl } from '../../../../../node_modules/@angular/forms';
import { Router, ActivatedRoute } from '../../../../../node_modules/@angular/router';
import { User } from '../../../models/user';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.scss']
})
export class EditUserComponent implements OnInit {

  username: string;
  password: string;
  
  constructor(private userService: UserService, private location: Location, private fb: FormBuilder, private activatedRoute: ActivatedRoute, private router: Router) { }

  ngOnInit() {
    this.activatedRoute.paramMap.subscribe(params => this.username = params.get('username'));
    this.getUser(this.username);
  }

  getUser(username) {
    this.userService.getUser(username).subscribe(result => {
      if (!result) {
        return;
      }
      console.log('result', result);
      if (result.result.username != this.username) {
        alert('username is not matched!!');
      }
    })
  }

  modifyUser() {
    var passwordObj = {'password': this.password};
    console.log('passwordObj = ', passwordObj, this.password);
    this.userService.modifyUser(this.username, passwordObj).subscribe(result => {
      if (!result) {
        return;
      }
      this.router.navigateByUrl('/user/users');
    }, 
    error => {
      console.log('modifyUser error', error);
      alert(error.error);
    });
  }

  deleteUser() {
    this.userService.removeUser(this.username).subscribe(result => {
      if (!result) {
        return;
      }
      this.router.navigateByUrl('/user/users');
    }, 
    error => {
      console.log('modifyUser error', error);
      alert(error.error);
    });
  }

  goBack() {
    this.location.back();
  }

}
