output = ""

while True:
	c = "{\n\t\"beginYear\":"
	b = input("First Year of Production: ")
	if b == "exit":
		print(output[:-2])
		break
	c += b;
	c += "\n\t\"lastYear\":"
	c += input("Last Year of Production: ")
	c += "\n\t\"chassisCode\":\""
	c += input("Chassis Code: ").upper()
	c += "\"\n}, "
	output += c
