homeController = ($scope, $http, $location) ->

	$scope.tableau = lignes: [{text: ""}]
	$scope.tempo=500
	$scope.lignesafficheur = ["", "", "", "", ""]
	$scope.clock = "initialised"
	$scope.intervalId = null
	$scope.active = null
	
	$http.get('/ffcanoe/courses').success (data) ->
		$scope.courses = data

	$scope.refreshPhase = ->
		$http.get('/ffcanoe/phases/' + $scope.course.id ).success (data) ->
			$scope.phases = data

	#
	# rafraichi l'affichage sur la page du contenu du tableau
	#

	$scope.refresh = ->
		if $scope.intervalId == null
			$scope.intervalId =
				setInterval ->
					$scope.$apply ->
						$scope.clock = Date.now()
						$http.get('/tableau/last').success (data) ->
							$scope.lignesafficheur = data
				, $scope.tempo

	#
	# arrete le rafraichissement auto du contenu du tableau
	#

	$scope.stop = ->
		$http.get('/tableau/stop')
		clearInterval($scope.intervalId)
		$scope.intervalId=null
		$scope.active=null

	#
	# efface le tableau
	#

	$scope.clear = ->
		$http.get('/tableau/clear')
		clearInterval($scope.intervalId)
		$scope.intervalId=null
		$scope.lignesafficheur = ["", "", "", "", ""]
		$scope.active=null

	#
	#
	#     gestion du Brouillon
	#
	#	$scope.brouillon.lignes
	#

	# destruction du brouillon
	$scope.effacer = ->
		$scope.brouillon = null

	# initiatilisation avec un modele
	$scope.modele = (mod)->
		$scope.brouillon = lignes: []
		$scope.brouillon.lignes = mod.template

	$scope.modeles = [
		{label: "FKT", template: [
			{text: "", center :"true"}
			{text: "Free Kayak Tour", center :"true"}
			{text: "Tournon 2014", center :"true"}
			{text: "", center :"true"}
		]}
		{label: "Départs", template: [ 
			{text: "", center :"true"}
			{text: "Free Kayak Tour", center :"true"}
			{text: "", center :"true"}
			{text: "Qualif K1HS", center :"true"}
			{text: "", center :"true"}
		]}
		{label: "Résultats", template: [ 
			{text: "", center :"true"}
			{text: "Free Kayak Tour", center :"true"}
			{text: "", center :"true"}
			{text: "Qualif K1HS", center :"true"}
			{text: "Résultats", center :"true"}
		]}
	]





	# ajout d'une ligne dans un tableau apres la ligne courante
	$scope.addLine = ($lignes, $ligne) ->
		$lignes.splice($lignes.indexOf($ligne)+1, 0,{text:""});

	# suppresssion de la ligne du tableau
	$scope.removeLine = ($lignes, $ligne) ->
		$lignes.splice($lignes.indexOf($ligne), 1);

	#
	#		Gestion des Toggles boutons
	#

	$scope.toggleScroll = ($ligne) ->
		$ligne.scroll = !$ligne.scroll

	$scope.toggleFlash = ($ligne) ->
		$ligne.flash = !$ligne.flash

	$scope.toggleCenter = ($ligne) ->
		$ligne.center = !$ligne.center

	$scope.toggleScrollTbl = ->
		$scope.tableau.scroll = !$scope.tableau.scroll

	$scope.toggleResultTbl = ->
		$scope.tableau.result = !$scope.tableau.result
		$scope.classement()

	$scope.toggleFlashTbl = ->
		$scope.tableau.flash = !$scope.tableau.flash

	$scope.toggleDebugTbl = ->
		$scope.tableau.debug = !$scope.tableau.debug


	#
	# remplissage de la liste avec les resultats ou les departs (sans rien afficher au tableau)
	#

	$scope.classement= ->
		$scope.runs = [1..$scope.phase.nbRun]
		if $scope.tableau.result
			$http.get('/ffcanoe/classement/' + $scope.course.id + '?phase=' + $scope.phase.typeManche).success (data) ->
				$scope.resultats = data
				if $scope.active != null
					$scope.affiche()
		else
			$http.get('/ffcanoe/depart/' + $scope.course.id + '?phase=' + $scope.phase.typeManche).success (data) ->
				$scope.resultats = data
				if $scope.active != null
					$scope.affiche()

	#
	# mise a jour d'une ligne des resultats (sans affichage)
	#
	
	$scope.majClassement= ($resultat) ->
		parameters = resultat: $resultat
		$http.get('/ffcanoe/maj', params: parameters).success ->
			$scope.classement()

	#
	# suppression d'une ligne de resultat
	#
	
	$scope.removeClassement = ($resultat) ->
		$scope.resultats.splice($scope.resultats.indexOf($resultat), 1)

	#
	# validation apres saisie d un dossard
	#
	
	$scope.validateDossard = ($resultat)->
		$http.get('/ffcanoe/dossard/' + $resultat.coureur.dossard ).success (data) ->
			if data.bateau
				$resultat.coureur = data
				$resultat.course = $scope.course
				$resultat.phase = $scope.phase
				$resultat.runs = []
				for i in [1..$scope.phase.nbRun]
					$resultat.runs.push { run : i, points: null } 

	#
	# import des nouvelles donnees avec maj des resultats
	#

	$scope.importFile= ->
		if $scope.active != -1
			for $res in $scope.resultats
				if $res.coureur.dossard == $scope.active
					codeCoureur=$res.coureur.codeCoureur
		
		parameters = { "filename": $scope.fileName, "phase" : $scope.phase.typeManche, "timeout" : $scope.autoTimer, "codeCoureur": codeCoureur}
		$http.get('/ffcanoe/import/' + $scope.course.id, params: parameters).success (data) ->
			if data != null && data != "" 
				$scope.lastImport=data
				$scope.lastImportStatus = "!" + data.coureur.dossard
				# ensuite on recherche la ligne si il y a un actif
				if $scope.active != null
					for $res in $scope.resultats
						if $res.coureur.dossard == data.coureur.dossard
							$scope.active = data.coureur.dossard
							# $scope.lastImportStatus = data.coureur.dossard
							# $scope.switchPaddle = $scope.autoWait
							# maintenant on verifie si tous ses runs sont valides
							valid=true
							for $run in data.runs
								if !$run.valid
									valid=false
							if valid
								$scope.lastImportStatus = data.coureur.dossard
								$scope.switchPaddle = $scope.autoWait
				# on met a jour le tableau
				$scope.classement()
			else
				$scope.lastImportStatus="-1"


	# ####################################
	#
	#	gestion du Tableau d'affichage
	#
	# ####################################

	#
	#	envoie du classement/liste de depart vers le tableau avec ajout de l'entete
	#
	
	$scope.affiche= ->
		$scope.tableau.lignes = []

		if $scope.active == null || $scope.active == -1
			# le brouillon (et encore)
			# ajout de l'entete
			if $scope.brouillon && $scope.brouillon.lignes.length != 0
				for $entete in $scope.brouillon.lignes
					$scope.tableau.lignes.push $entete

		if $scope.active == -1
			for $res in $scope.resultats
				if $scope.tableau.result
					# en mode resultats
					# 
					$scope.tableau.lignes.push { text: ($res.classement|| ' ') + ') ' + $res.coureur.dossard + '-' + $res.coureur.bateau}
					# $scope.tableau.lignes.push { text: '-   Points : ' + $res.totalManche}
				else
					# en mode liste de depart
					# 
					$scope.tableau.lignes.push { text: $res.coureur.dossard + '-' + $res.coureur.bateau}
					# $scope.tableau.lignes.push { text: 'Pts : ' + $res.totalManche + '(' + ($res.classement|| '_') + "/" + $scope.phase.nbQualifies+ ')'}
					# $scope.tableau.lignes.push { text: ''}
		
		if $scope.active != -1
			# on le cherche et on l'affiche
			for $res in $scope.resultats
				if $res.coureur.dossard == $scope.active
					$scope.buildDetail($res)
			
		# parameters = tableau: $scope.tableau
		
		$http.post('/tableau/refresh', $scope.tableau)
		$scope.refresh()

	#
	# choix du mode d'affichage (detail/liste)
	#
	#	=> $scope.active : Num dossard pour un detail
	#						-1 pour le tableau (depart ou arrivee)
	#						null pour rien
	
	$scope.toggleDetail= ($res)->
		if $scope.active != $res.coureur.dossard
			$scope.active = $res.coureur.dossard
			$scope.tableau.scroll = false
			$scope.affiche()
		else
			$scope.active = null
	
	$scope.toggleClassementTbl= ->
		if $scope.active != -1
			$scope.active = -1
			$scope.tableau.scroll = true
			$scope.affiche()
		else 
			$scope.active = null

	#
	#
	#   affichage automatique
	#
	#
	$scope.autoTimer = 5
	$scope.autoWait = 30

	$scope.toggleAutoTbl= ->
		$scope.auto = !$scope.auto
		if $scope.auto
			if ! $scope.autoId
				$scope.autoId =
					setInterval ->
						$scope.$apply ->
							$scope.autoClock = Date.now()
							$scope.importFile()
							# si le dernier import a plus de n mn => on passe au suivant
							# sauf qu'il faudrait changer uniquement si on vient de valider un run !!
							if $scope.switchPaddle != null
								$scope.switchPaddle =  $scope.switchPaddle - $scope.autoTimer
								if $scope.switchPaddle < 0 
									$scope.switchPaddle = null
									$scope.next($scope.lastImport.coureur.dossard)
					, $scope.autoTimer * 1000
		else
			clearInterval($scope.autoId)
			$scope.autoId=null

	
	# 
	# recherche du concurrent suivant
	# tout est dans lastImport
	#
	
	$scope.next = (previous)->
		bpp = $scope.phase.boatPerPool
		for res, i in $scope.resultats
			if res.coureur.dossard == previous
				# alert "on reste dans la poule ?"
				if res.runs[$scope.phase.nbRun-1].points
					# alert 'dernier run couru donc on passe a la poule suivante' + i
					if i+1 < $scope.resultats.length
						next = $scope.resultats[i+1]
					else
						alert "on est sur le dernier"
						next = res
				else
					# alert "on reste dans la poule " + i
					# petit calcul de modulo
					if (i+1) % bpp == 0 
						# on est en fin de poule 
						next = $scope.resultats[i-bpp+1]
					else 
						# et si c est la derniere poule ... 
						if i+1 < $scope.resultats.length
							next = $scope.resultats[i+1]
						else
							next = $scope.resultats[ (i - i % bpp) ]
		
		# alert 'le suivant est ' + next.coureur.dossard
		$scope.active=next.coureur.dossard
		$scope.affiche()


	#
	# envoie du detail d'un competiteur au tableau
	#
	
	$scope.buildDetail= ($res) ->
	
		# envoi direct au tableau
		$scope.tableau.lignes = []
		$scope.tableau.lignes.push { text: $res.coureur.dossard + ' ' + $res.coureur.bateau }
		$scope.tableau.lignes.push { text: ($res.coureur.club || "nullepart") + ' - ' + $res.coureur.club, scroll: true }
		$scope.tableau.lignes.push { text: ' '}

		$strRun = ''
		$nbRun=1
		for $run in $res.runs
			# $strRun = $strRun + 'R' + $nbRun + ':' + ($run.points || "___") + ' '
			$strRun = $strRun + 'R' + $nbRun + ':' + (($run.valid && $run.points) || "__") + ' '
			$nbRun = $nbRun+1
		if $strRun.length > 20
			$scope.tableau.lignes.push { text: $strRun, scroll: true }
		else
			$scope.tableau.lignes.push { text: $strRun }
		$scope.tableau.lignes.push { 
			text: 'Tot: '+ ($res.totalManche || "___") + ' Cl: '+ ($res.classement || "_") + "/" + $scope.phase.nbQualifies
		}
			
			