document.addEventListener("DOMContentLoaded", (event) => {
  document
    .getElementById("submitButton")
    .setAttribute("onclick", "FetchSubmit(event)");
});

async function FetchSubmit(e) {
  e.preventDefault();
  var searchData = Seach_Box.text.value;
  console.log(searchData);

  var response = await fetch("/bin/searchbox1", {
    method: "POST",
    body: searchData,
    headers: {
      "Content-type": "application/json; charset=UTF-8",
    },
  });
  var output = await response.json();
  console.log(output);
  var contetelem = document.getElementById("jsonContent");

  let contentString = "";
  if (output.length == 0) {
    alert("Your term returned 0 results");
    return;
  } else {
    output.forEach((element) => {
      contentString += `<div class="textColor text-align-center">
      <h1>${element.title}</h1>
      <h3>${element.lastModified}</h3>
      <p>${element.description}</p>
      <img src="${element.image}"/>
      </div>`;
    });
    contetelem.innerHTML = contentString;
  }
}
