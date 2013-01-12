class Group < ActiveRecord::Base
  attr_accessible :owner_id
  belongs_to :owner, class_name: "User"
end
