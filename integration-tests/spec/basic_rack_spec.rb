require 'container'
require 'open-uri'

describe "basic rack test" do
  include TorqueBox::TestContainer
  before(:all) do
    start "rack/1.1.0/basic-rack.yml"
  end
  after(:all) do
    stop
  end

  it "should not get a 500 server error" do
    result = open("http://localhost:8080/basic-rack").read
  end

end