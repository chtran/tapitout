class SessionsController < Devise::SessionsController
  def create
    respond_to do |format|
      format.html { super }
      format.xml {
        warden.authenticate!(:scope => resource_name, :recall => "#{controller_path}#new")
        render :status => 200, :xml => { :session => { :error => "Success", :auth_token => current_user.authentication_token}}
      }
end
