require 'xmlrpc/client'

url = ARGV[0]
project_name = ARGV[1]

puts "Nudging DamageControl on #{url} to build project #{project_name}"
client = XMLRPC::Client.new2(url)
build = client.proxy("build")
result = build.trig(project_name, Time.now.utc.strftime("%Y%m%d%H%M%S"))
puts result
