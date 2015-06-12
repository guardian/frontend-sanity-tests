#!/usr/bin/env ruby

# this file is copied to the linter directory once it's downloaded

$:.unshift(File.expand_path('../lib',  __FILE__))

require 'rubygems' || Gem.clear_paths
require 'bundler'
Bundler.setup(:default)

require 'restclient/components'
require 'rdf/linter'

puts RDF::Linter::Parser.parse({:base_uri => ARGV[0], :logger => Logger.new(STDERR)})[1].map {|k, v| v.map {|o, mm| Array(mm).map {|m| "#{k} #{o}: #{m}"}}}.flatten.to_json
