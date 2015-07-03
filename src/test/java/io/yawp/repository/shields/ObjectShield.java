package io.yawp.repository.shields;

import io.yawp.repository.IdRef;
import io.yawp.repository.models.basic.ShieldedObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

public class ObjectShield extends Shield<ShieldedObject> {

	@Override
	protected void defaults() {
		allow(isJim());
	}

	@Override
	protected void show(IdRef<ShieldedObject> id) {
		allow(isJane());
		defaults();
	}

	@Override
	protected void update(ShieldedObject object) {
		allow(isJane());
		defaults();
	}

	@Override
	protected void custom() {
		allow(isJane());
		defaults();
	}

	private boolean isJane() {
		return is("jane@rock.com");
	}

	private boolean isJim() {
		return is("jim@rock.com");
	}

	private boolean is(String email) {
		User currentUser = UserServiceFactory.getUserService().getCurrentUser();
		return currentUser != null && currentUser.getEmail().equals(email);
	}

}